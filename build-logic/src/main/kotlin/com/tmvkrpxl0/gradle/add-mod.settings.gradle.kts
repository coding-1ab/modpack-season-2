package com.tmvkrpxl0.gradle

val extension: AddModExtension = extensions.create("addMods", AddModExtension::class.java, settings)
val modules = extension.projectPrimaryModuleIds

gradle.lifecycle.beforeProject(LifetimeHookAction(modules))
