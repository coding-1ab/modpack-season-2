pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "codinglab-modpack-1.21.1"

fun addMod(path: String, module: String, projectName: String = ":") {
    includeBuild(path) {
        dependencySubstitution {
            substitute(module(module)).withoutArtifactSelectors().using(project(projectName))
        }
    }
}

addMod("./mods/Create", "com.simibubi.create:create-1.21.1")
addMod("./mods/create_connected", "com.hlysine.create_connected:create_connected")
addMod("./mods/createaddition", "com.mrh0.createaddition:createaddition")
addMod("./mods/AppleSkin", "squeek.appleskin:appleskin")
// addMod("./mods/create-new-age", "org.antarcticgardens.cna:create-new-age", ":neoforge")
addMod("./mods/Applied-Energistics-2", "org.appliedenergistics:appliedenergistics2")

includeBuild("./mods/JustEnoughItems") {
    dependencySubstitution {
        substitute(module("mezz.jei:jei-1.21.1-neoforge")).withoutArtifactSelectors().using(project(":NeoForge"))
        substitute(module("mezz.jei:jei-1.21.1-neoforge-api")).withoutArtifactSelectors().using(project(":NeoForgeApi"))
        substitute(module("mezz.jei:jei-1.21.1-common")).withoutArtifactSelectors().using(project(":Common"))
        substitute(module("mezz.jei:jei-1.21.1-common-api")).withoutArtifactSelectors().using(project(":CommonApi"))
        substitute(module("mezz.jei:jei-1.21.1-fabric")).withoutArtifactSelectors().using(project(":Fabric"))
        substitute(module("mezz.jei:jei-1.21.1-fabric-api")).withoutArtifactSelectors().using(project(":FabricApi"))
    }
}

startParameter.excludedTaskNames += ":JustEnoughItems:Forge:createMcpToSrg"
startParameter.excludedTaskNames += ":JustEnoughItems:ForgeApi:createMcpToSrg"
