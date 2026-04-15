pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.tmvkrpxl0.gradle.add-mod")
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "codinglab-modpack-1.21.1"

addMods {
    addMod("./mods/Create", "com.simibubi.create:create-1.21.1")
    addMod("./mods/create_connected", "com.hlysine.create_connected:create_connected")
    addMod(
        "./mods/createaddition", arrayOf(
            "com.mrh0.createaddition:createaddition" to ":",
            "maven.modrinth:createaddition" to ":",
        )
    )
    addMod("./mods/AppleSkin", "squeek.appleskin:appleskin-neoforge")
    addMod("./mods/create-new-age", "org.antarcticgardens.cna:create-new-age", ":neoforge")
    addMod("./mods/Applied-Energistics-2", "org.appliedenergistics:appliedenergistics2")
    addMod("./mods/NatureSpiritForge", "net.hibiscus:natures_spirit")
    addMod(
        "./mods/FarmersDelight", arrayOf(
            "vectorwing.farmersdelight:FarmersDelight" to ":",
            "maven.modrinth:farmers-delight" to ":",
        )
    )
    addMod("./mods/Spice-of-Life-Carrot-Edition", "com.cazsius.solcarrot:solcarrot")
    addMod("./mods/CreateDragonPlus", "plus.dragons.createdragonsplus:create-dragons-plus-1.21.1")
    addMod("./mods/CreateEnchantmentIndustry", "plus.dragons.createenchantmentindustry:create-enchantment-industry-1.21.1")
    addMod("./mods/Quicksand", "plus.dragons.quicksand:quicksand-neoforge-1.21.1")
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
}
