package dev.codinglabs.gradle

import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ListProperty

abstract class AddModExtension(private val settings: Settings) {
    abstract val registeredProjects: ListProperty<ModConfig>

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
