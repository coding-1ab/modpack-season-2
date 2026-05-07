package dev.codinglabs.gradle

val extension: AddModExtension = extensions.create("addMods", AddModExtension::class.java, settings)
val modules = extension.registeredProjects
val extras = extension.extraMods

gradle.lifecycle.beforeProject(BeforeProjectAction(modules, extras))
