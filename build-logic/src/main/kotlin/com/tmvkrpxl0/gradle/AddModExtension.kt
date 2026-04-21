package com.tmvkrpxl0.gradle

import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ListProperty

abstract class AddModExtension(private val settings: Settings) {
    abstract val registeredProjects: ListProperty<ModConfig>

    fun addMod(path: String, module: String, projectPath: String = ":") {
        val config = ModConfig(
            filePath = path,
            includeTransitive = true,
            excludeAsset = false,
            modProjects = listOf(
                ModProjectConfig(
                    dependencyNotations = listOf(module),
                    projectPath,
                    shouldUnpack = true
                )
            )
        )
        addMod(config)
    }

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
        excludeAsset: Boolean = false,
    ) {
        val mod = ModConfig(filePath, includeTransitive, modProjects, excludeAsset)
        addMod(mod)
    }

    fun mod(
        filePath: String,
        includeTransitive: Boolean,
        modProjects: List<ModProjectConfig>,
        excludeAsset: Boolean = false,
    ) = ModConfig(filePath, includeTransitive, modProjects, excludeAsset)

    fun modProject(
        dependencyNotations: List<String>,
        projectPath: String,
        shouldUnpack: Boolean
    ) = ModProjectConfig(dependencyNotations, projectPath, shouldUnpack)
}

data class ModConfig(
    val filePath: String,
    val includeTransitive: Boolean,
    val modProjects: List<ModProjectConfig>,
    val excludeAsset: Boolean
)

data class ModProjectConfig(
    val dependencyNotations: List<String>,
    val projectPath: String,
    val shouldUnpack: Boolean,
)
