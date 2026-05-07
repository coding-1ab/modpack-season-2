package dev.codinglabs.gradle

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import org.apache.http.impl.client.HttpClientBuilder
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ListProperty
import java.lang.reflect.Type
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

abstract class AddModExtension(private val settings: Settings) {
    abstract val registeredProjects: ListProperty<ModConfig>
    abstract val extraMods: ListProperty<String>

    fun addMod(config: ModConfig) {
        registeredProjects.add(config)

        settings.includeBuild(config.filePath) {
            dependencySubstitution {
                for (modProject in config.modProjects) {
                    for (notation in modProject.dependencyNotations) {
                        substitute(module(notation))
                            .withoutArtifactSelectors()
                            .using(project(modProject.projectPath))
                    }
                }
            }
        }
    }

    fun addMod(
        filePath: String,
        includeTransitive: Boolean,
        modProjects: List<ModProjectConfig>,
        assetSource: AssetSource = AssetSource.Inline,
    ) {
        val mod = ModConfig(filePath, includeTransitive, modProjects, assetSource)
        addMod(mod)
    }

    fun modProject(
        dependencyNotations: List<String>,
        projectPath: String,
        shouldUnpack: Boolean
    ) = ModProjectConfig(dependencyNotations, projectPath, shouldUnpack)

    fun inline() = AssetSource.Inline
    fun curseforge(projectId: String, fileId: String) = AssetSource.CurseForge(projectId, fileId)
    fun modrinth(version: String) = AssetSource.Modrinth(version)
    fun includeExtra(extraPath: String) {
        val contents = settings.rootDir.resolve(extraPath).readText(StandardCharsets.UTF_8)
        val type: Type = object: TypeToken<MutableMap<String, Any?>>() {}.type
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
        contents.lineSequence().filter { line -> line.isNotEmpty() }.forEach { line ->
            val tokens = line.split(" ")
            when(tokens[0]) {
                "curseforge" -> {
                    val projectId = tokens[1]
                    val fileId = tokens[2]
                    val dependency = "curse.maven:extra-$projectId:$fileId"
                    extraMods.add(dependency)
                }
                "modrinth" -> {
                    val version = tokens[1]
                    val url = "https://api.modrinth.com/v3/version/$version"
                    val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
                    val jsonInput = client.send(request, HttpResponse.BodyHandlers.ofString())
                    val json = jsonInput.body()
                    val gson = Gson()
                    val parsed: MutableMap<String, String> = try {
                        gson.fromJson(json, type)
                    } catch (e: Exception) {
                        throw RuntimeException("Failed to parse modrinth json. Contents:\n$json", e)
                    }
                    val projectId = parsed["project_id"]
                    extraMods.add("maven.modrinth:$projectId:$version")
                }
                else -> {
                    throw IllegalArgumentException("Unexpected token: $line")
                }
            }
        }
    }
}

data class ModConfig(
    val filePath: String,
    val includeTransitive: Boolean,
    val modProjects: List<ModProjectConfig>,
    val assetSource: AssetSource
)

sealed interface AssetSource {
    object Inline: AssetSource
    data class CurseForge(val projectId: String, val fileId: String): AssetSource
    data class Modrinth(val version: String): AssetSource
}

data class ModProjectConfig(
    val dependencyNotations: List<String>,
    val projectPath: String,
    val shouldUnpack: Boolean,
)
