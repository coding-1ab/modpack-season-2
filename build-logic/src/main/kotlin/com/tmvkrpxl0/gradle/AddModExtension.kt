package com.tmvkrpxl0.gradle

import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ListProperty

abstract class AddModExtension(private val settings: Settings) {
    abstract val registeredProjects: ListProperty<ModConfig>

    fun addMod(path: String, module: String, projectPath: String = ":") {
        val config = ModConfig(
            filePath = path,
            includeTransitive = true,
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
        modProjects: List<ModProjectConfig>
    ) {
        val mod = ModConfig(filePath, includeTransitive, modProjects)
        addMod(mod)
    }

    fun mod(
        filePath: String,
        includeTransitive: Boolean,
        modProjects: List<ModProjectConfig>
    ) = ModConfig(filePath, includeTransitive, modProjects)

    fun modProject(
        dependencyNotations: List<String>,
        projectPath: String,
        shouldUnpack: Boolean
    ) = ModProjectConfig(dependencyNotations, projectPath, shouldUnpack)
}

data class ModConfig(
    val filePath: String,
    val includeTransitive: Boolean,
    val modProjects: List<ModProjectConfig>
)

data class ModProjectConfig(
    val dependencyNotations: List<String>,
    val projectPath: String,
    val shouldUnpack: Boolean,
)
