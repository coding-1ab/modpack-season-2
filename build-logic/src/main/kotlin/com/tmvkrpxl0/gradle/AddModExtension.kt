package com.tmvkrpxl0.gradle

import org.gradle.api.artifacts.DependencySubstitutions
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ListProperty

abstract class AddModExtension(private val settings: Settings) {
    abstract val registeredProjects: ListProperty<String>
    abstract val projectPrimaryModuleIds: ListProperty<Pair<String, Boolean>>

    fun addMod(path: String, module: String, projectName: String = ":", extra: DependencySubstitutions.() -> Unit = {}) {
        val substitutions = arrayOf(module to projectName)
        addMod(path, substitutions, true)
    }

    fun addMod(path: String, substitutions: Array<Pair<String, String>>, includeTransitive: Boolean, extra: DependencySubstitutions.() -> Unit = {}) {
        registeredProjects.add(path)
        projectPrimaryModuleIds.add(substitutions.first().first to includeTransitive)

        settings.includeBuild(path) {
            dependencySubstitution {
                extra()
                for ((coordinate, projectName) in substitutions.iterator()) {
                    substitute(module(coordinate))
                        .withoutArtifactSelectors()
                        .using(project(projectName))
                }
            }
        }
    }
}
