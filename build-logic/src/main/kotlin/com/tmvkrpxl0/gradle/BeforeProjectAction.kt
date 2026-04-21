package com.tmvkrpxl0.gradle

import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.language.jvm.tasks.ProcessResources

@Suppress("UnstableApiUsage")
class BeforeProjectAction(
    private val mods: ListProperty<ModConfig>,
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
            val createModJars = project.tasks.register<Jar>("createModJars")

            mods.get().forEach { mod ->
                val includeTransitive = mod.includeTransitive
                val excludeAssets = mod.excludeAsset

                mod.modProjects.forEach { modProject ->
                    val notation = modProject.dependencyNotations.first()
                    val shouldUnpack = modProject.shouldUnpack
                    if (!shouldUnpack) {
                        return@forEach
                    }

                    val dependency = project.dependencies.create(notation) as ModuleDependency
                    val separator = notation.indexOf(':')
                    val artifactId = notation.substring(separator + 1)
                    val sourceSetName = artifactId.replace('.', '-')
                    val sourceArtifactContainer = "${sourceSetName}nonTransitive"
                    val sourceArtifactContainerConfig = project.configurations.create(sourceArtifactContainer) {
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

                        project.dependencies.add(sourceArtifactContainer, dependency) {
                            isTransitive = false
                        }

                        val unzipped = sourceArtifactContainerConfig.elements.map { elements ->
                            val jar = elements.singleOrNull()!!
                            archive.zipTree(jar)
                        }

                        project.tasks.named<ProcessResources>(processResourcesTaskName) {
                            from(unzipped)
                            duplicatesStrategy = DuplicatesStrategy.WARN
                        }

                        val prepareJar = project.tasks.register<Jar>("${sourceSetName}prepareJar") {
                            from(unzipped)
                            duplicatesStrategy = DuplicatesStrategy.WARN
                            if (excludeAssets) {
                                exclude("assets")
                            }
                        }

                        createModJars.configure {
                            dependsOn(prepareJar)
                        }
                    }
                }
            }

            val transitiveOnly = transitive.minus(nonTransitive)
            project.dependencies.add("localRuntime", transitiveOnly)
        }
    }
}
