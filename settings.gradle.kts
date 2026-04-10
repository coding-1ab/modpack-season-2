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
    addMod(path, arrayOf(module to projectName))
}

fun addMod(path: String, substitutions: Array<Pair<String, String>>) {
    includeBuild(path) {
        for ((coordinate, projectName) in substitutions.iterator()) {
            dependencySubstitution {
                substitute(module(coordinate)).withoutArtifactSelectors().using(project(projectName))
            }
        }
    }
}

addMod("./mods/Create", "com.simibubi.create:create-1.21.1")
addMod("./mods/create_connected", "com.hlysine.create_connected:create_connected")
addMod("./mods/createaddition", "com.mrh0.createaddition:createaddition")
addMod("./mods/AppleSkin", "squeek.appleskin:appleskin")
addMod("./mods/create-new-age", "org.antarcticgardens.cna:create-new-age", ":neoforge")
addMod("./mods/Applied-Energistics-2", "org.appliedenergistics:appliedenergistics2")
addMod("./mods/NatureSpiritForge", "net.hibiscus:natures_spirit")
addMod(
    "./mods/CC-Tweaked", arrayOf(
        "cc.tweaked:cc-tweaked-1.21.1-common-api" to ":common-api",
        "cc.tweaked:cc-tweaked-1.21.1-common" to ":common",
        "cc.tweaked:cc-tweaked-1.21.1-forge-api" to ":forge-api",
        "cc.tweaked:cc-tweaked-1.21.1-forge" to ":forge",
        "cc.tweaked:cc-tweaked-1.21.1-core-api" to ":core-api",
        "cc.tweaked:cc-tweaked-1.21.1-core" to ":core",
    )
)
