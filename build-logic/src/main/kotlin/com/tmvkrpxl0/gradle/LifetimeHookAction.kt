package com.tmvkrpxl0.gradle

import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.register
import org.gradle.language.jvm.tasks.ProcessResources

@Suppress("UnstableApiUsage")
class LifetimeHookAction(
    private val modules: ListProperty<String>,
) : IsolatedAction<Project> {
    override fun execute(target: Project) {
        target.pluginManager.withPlugin("java") {
            val sourceSet = target.extensions.getByType(SourceSetContainer::class.java)
            target.configurations.maybeCreate("localRuntime")
            val nonTransitive = target.configurations.create("nonTransitive") {
                isTransitive = false
            }
            val transitive = target.configurations.create("transitive") {
                isTransitive = true
            }
            val main = sourceSet.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

            modules.get().forEach { module ->
                val dependency = target.dependencies.create(module) as ModuleDependency
                val separator = module.indexOf(':')
                val artifactId = module.substring(separator + 1)
                val sourceSetName = artifactId.replace('.', '-')
                sourceSet.create(sourceSetName) {
                    target.dependencies.add(this.implementationConfigurationName, dependency)
                    target.dependencies.add("localRuntime", this.output)

                    target.dependencies.add("nonTransitive", dependency) {
                        isTransitive = false
                    }
                    target.dependencies.add("transitive", dependency) {
                        isTransitive = true
                    }

                    target.tasks.register<ProcessResources>("copy${sourceSetName}Resources") {
                        doLast {
                            main.resources.srcDirs.addAll(nonTransitive)
                        }
                    }
                }
            }

            val transitiveOnly = transitive.minus(nonTransitive)
            target.dependencies.add("localRuntime", transitiveOnly)
        }
    }
}
