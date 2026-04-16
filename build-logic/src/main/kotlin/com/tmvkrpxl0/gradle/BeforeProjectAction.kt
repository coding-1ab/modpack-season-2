package com.tmvkrpxl0.gradle

import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.language.jvm.tasks.ProcessResources

@Suppress("UnstableApiUsage")
class BeforeProjectAction(
    private val modules: ListProperty<Pair<String, Boolean>>,
) : IsolatedAction<Project> {
    override fun execute(project: Project) {
        val archive = project.serviceOf<ArchiveOperations>()

        project.pluginManager.withPlugin("java") {
            val sourceSet = project.extensions.getByType(SourceSetContainer::class.java)
            project.configurations.maybeCreate("localRuntime")
            val transitive = project.configurations.create("transitive") {
                isTransitive = true
            }
            val nonTransitive = project.configurations.create("nonTransitive") {
                isTransitive = false
            }
            modules.get().forEach { (module, includeTransitive) ->
                val dependency = project.dependencies.create(module) as ModuleDependency
                val separator = module.indexOf(':')
                val artifactId = module.substring(separator + 1)
                val sourceSetName = artifactId.replace('.', '-')
                val moduleNonTransitive = project.configurations.create("${sourceSetName}nonTransitive") {
                    isTransitive = false
                }

                sourceSet.register(sourceSetName) {
                    val moduleOutput = this.output
                    project.dependencies.add("localRuntime", moduleOutput)

                    if (includeTransitive) {
                        project.dependencies.add("nonTransitive", dependency) {
                            isTransitive = false
                        }
                        project.dependencies.add("transitive", dependency) {
                            isTransitive = true
                        }
                    }

                    project.dependencies.add("${sourceSetName}nonTransitive", dependency) {
                        isTransitive = false
                    }

                    val unzipped = moduleNonTransitive.elements.map { elements ->
                        val jar = elements.singleOrNull()!!
                        archive.zipTree(jar)
                    }

                    project.tasks.named<ProcessResources>(processResourcesTaskName) {
                        from(unzipped)
                        duplicatesStrategy = DuplicatesStrategy.WARN
                    }
                }
            }

            val transitiveOnly = transitive.minus(nonTransitive)
            project.dependencies.add("localRuntime", transitiveOnly)
        }
    }
}
