package com.tmvkrpxl0.gradle

import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.language.jvm.tasks.ProcessResources

@Suppress("UnstableApiUsage")
class BeforeProjectAction(
    private val modules: ListProperty<String>,
) : IsolatedAction<Project> {
    override fun execute(project: Project) {
        val archive = project.serviceOf<ArchiveOperations>()

        project.pluginManager.withPlugin("net.neoforged.moddev") {
            val neoForge = project.extensions.getByType<NeoForgeExtension>()
            project.pluginManager.withPlugin("java") {
                val sourceSet = project.extensions.getByType(SourceSetContainer::class.java)
                project.configurations.maybeCreate("localRuntime")
                val transitive = project.configurations.create("transitive") {
                    isTransitive = true
                }
                val nonTransitive = project.configurations.create("nonTransitive") {
                    isTransitive = false
                }
                modules.get().forEach { module ->
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

                        project.dependencies.add("nonTransitive", dependency) {
                            isTransitive = false
                        }
                        project.dependencies.add("${sourceSetName}nonTransitive", dependency) {
                            isTransitive = false
                        }
                        project.dependencies.add("transitive", dependency) {
                            isTransitive = true
                        }

                        val unzipped = moduleNonTransitive.elements.map { elements ->
                            val jar = elements.singleOrNull()!!
                            archive.zipTree(jar)
                        }

                        project.tasks.named<ProcessResources>(processResourcesTaskName) {
                            from(unzipped)
                        }
                    }
                }

                val transitiveOnly = transitive.minus(nonTransitive)
                project.dependencies.add("localRuntime", transitiveOnly)
            }
        }
    }
}
