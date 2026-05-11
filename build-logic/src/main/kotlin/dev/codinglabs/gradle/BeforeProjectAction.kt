package dev.codinglabs.gradle

import org.gradle.api.IsolatedAction
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
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
    private val extras: ListProperty<ExtraModConfig>
) : IsolatedAction<Project> {
    override fun execute(project: Project) {
        val archive = project.serviceOf<ArchiveOperations>()
        val isRoot = project.rootProject == project

        project.pluginManager.withPlugin("java") {
            val sourceSet = project.extensions.getByType(SourceSetContainer::class.java)
            project.configurations.maybeCreate("localRuntime")
            val transitive = project.configurations.create("transitive") {
                isTransitive = true
            }
            val nonTransitive = project.configurations.create("nonTransitive") {
                isTransitive = false
            }

            val jarDestination = project.file("createdModJars")
            val createModJars = if (isRoot) {
                val clearModJars = project.tasks.register<Delete>("clearModJars") {
                    delete(jarDestination)
                }
                project.tasks.register<Copy>("createModJars") {
                    mustRunAfter(clearModJars)

                    val jarTask = project.tasks.named<Jar>("jar")
                    dependsOn(jarTask)
                    project.subprojects.forEach {
                        val subJarTask = it.tasks.named<Jar>("shadowJar")
                        dependsOn(subJarTask)
                        from(subJarTask.get().archiveFile)
                    }

                    from(project.projectDir.resolve("extra_mods.txt"))
                    from(jarTask.get().archiveFile)
                    into(jarDestination)
                }
            } else {
                null
            }

            mods.get().forEach { mod ->
                val includeTransitive = mod.includeTransitive
                val assetSource = mod.assetSource

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

                        if (isRoot) {
                            val prepareJar = project.tasks.register<Jar>("${sourceSetName}prepareJar") {
                                from(unzipped) {
                                    if (assetSource !is AssetSource.Inline) {
                                        exclude("assets")
                                        exclude("assets/*")
                                        exclude("assets/**")
                                        archiveClassifier.set("trimmed")
                                    }
                                }
                                duplicatesStrategy = DuplicatesStrategy.WARN
                                archiveBaseName.set(artifactId)

                                destinationDirectory.set(jarDestination)
                            }

                            createModJars!!.configure {
                                dependsOn(prepareJar)

                                doLast {
                                    when(assetSource) {
                                        is AssetSource.CurseForge -> {
                                            val file = jarDestination.resolve("$artifactId.assetSource")
                                            file.writeText("curseforge ${assetSource.projectId} ${assetSource.fileId}")
                                        }
                                        is AssetSource.Modrinth -> {
                                            val file = jarDestination.resolve("$artifactId.assetSource")
                                            file.writeText("modrinth ${assetSource.version}")
                                        }
                                        is AssetSource.Inline -> {}
                                    }
                                }
                            }
                        }

                    }
                }
            }

            val transitiveOnly = transitive.minus(nonTransitive)
            project.dependencies.add("localRuntime", transitiveOnly)

            for (extra in extras.get()) {
                if (extra.devExclude) {
                    continue
                }
                project.dependencies.add("localRuntime", extra.dependency) {
                    isTransitive = false
                }
            }
        }
    }
}
