pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.muon.rip/releases") }
    }
}

plugins {
    id("dev.codinglabs.gradle.add-mod")
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "codinglab-modpack-1.21.1"

include("updater")

addMods {
    addMod(
        filePath = "./mods/Create",
        includeTransitive = false,
        assetSource = modrinth("UjX6dr61"),
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.simibubi.create:create-1.21.1"),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/create_connected",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.hlysine.create_connected:create_connected"),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/createaddition",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.mrh0.createaddition:createaddition", "maven.modrinth:createaddition"),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/AppleSkin",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("squeek.appleskin:appleskin-neoforge"),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/create-new-age",
        includeTransitive = false,
        assetSource = modrinth("eQ9rbApE"),
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("org.antarcticgardens.cna:create-new-age"),
                projectPath = ":neoforge",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/Applied-Energistics-2",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf(
                    "org.appliedenergistics:appliedenergistics2",
                    "curse.maven:applied-energistics-2-223794"
                ),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/NatureSpiritForge",
        includeTransitive = false,
        assetSource = modrinth("MGjl80vc"),
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("net.hibiscus:natures_spirit"),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/FarmersDelight",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf(
                    "vectorwing.farmersdelight:FarmersDelight",
                    "maven.modrinth:farmers-delight"
                ),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/Spice-of-Life-Carrot-Edition",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.cazsius.solcarrot:solcarrot"),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/CreateDragonPlus",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf(
                    "plus.dragons.createdragonsplus:create-dragons-plus",
                    "plus.dragons.createdragonsplus:create-dragons-plus-1.21.1"
                ),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/CreateEnchantmentIndustry",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf(
                    "plus.dragons.createenchantmentindustry:create-enchantment-industry",
                    "plus.dragons.createenchantmentindustry:create-enchantment-industry-1.21.1",
                ),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )
    addMod(
        filePath = "./mods/Quicksand",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf(
                    "plus.dragons.quicksand:quicksand",
                    "plus.dragons.quicksand:quicksand-neoforge-1.21.1",
                ),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )

    addMod(
        filePath = "./mods/CC-Tweaked",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("cc.tweaked:cc-tweaked-1.21.1-forge"),
                projectPath = ":forge",
                shouldUnpack = true,
            ),
            modProject(
                dependencyNotations = listOf("cc.tweaked:cc-tweaked-1.21.1-forge-api"),
                projectPath = ":forge-api",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("cc.tweaked:cc-tweaked-1.21.1-common"),
                projectPath = ":common",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("cc.tweaked:cc-tweaked-1.21.1-common-api"),
                projectPath = ":common-api",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("cc.tweaked:cc-tweaked-1.21.1-core"),
                projectPath = ":core",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("cc.tweaked:cc-tweaked-1.21.1-core-api"),
                projectPath = ":core-api",
                shouldUnpack = false,
            )
        ),
    )
    addMod(
        filePath = "./mods/iteminteractions",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("fuzs.iteminteractions:iteminteractions"),
                projectPath = ":",
                shouldUnpack = true,
            )
        ),
    )
    addMod(
        filePath = "./mods/Create-Stock-Bridge/NeoForge",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.tom5454.createstockbridge:createstockbridge-1.21.1"),
                projectPath = ":",
                shouldUnpack = true
            )
        ),
    )
    addMod(
        filePath = "./mods/Simulated-Project",
        includeTransitive = false,
        assetSource = modrinth("YhZLrAFC"),
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("dev.simulated_team.simulated:create-aeronautics-bundled-1.21.1"),
                projectPath = ":aeronautics-bundled",
                shouldUnpack = true,
            ),
            modProject(
                dependencyNotations = listOf(
                    "dev.simulated_team.simulated:aeronautics-neoforge-1.21.1",
                    "dev.eriksonn.aeronautics:aeronautics-neoforge-1.21.1"
                ),
                projectPath = ":aeronautics:neoforge",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf(
                    "dev.simulated_team.simulated:aeronautics-common-1.21.1",
                    "dev.eriksonn.aeronautics:aeronautics-common-1.21.1"
                ),
                projectPath = ":aeronautics:common",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("dev.simulated_team.simulated:offroad-neoforge-1.21.1"),
                projectPath = ":offroad:neoforge",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("dev.simulated_team.simulated:offroad-common-1.21.1"),
                projectPath = ":offroad:common",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("dev.simulated_team.simulated:simulated-neoforge-1.21.1"),
                projectPath = ":simulated:neoforge",
                shouldUnpack = false,
            ),
            modProject(
                dependencyNotations = listOf("dev.simulated_team.simulated:simulated-common-1.21.1"),
                projectPath = ":simulated:common",
                shouldUnpack = false,
            ),
        ),
    )
    addMod(
        filePath = "./mods/sable",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("dev.ryanhcode.sable:sable-neoforge-1.21.1"),
                projectPath = ":neoforge",
                shouldUnpack = true,
            ),
            modProject(
                dependencyNotations = listOf("dev.ryanhcode.sable:sable-common-1.21.1"),
                projectPath = ":common",
                shouldUnpack = false,
            ),
        )
    )
    addMod(
        filePath = "./mods/Veil",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("foundry.veil:veil-neoforge-1.21.1"),
                projectPath = ":neoforge",
                shouldUnpack = true,
            ),
            modProject(
                dependencyNotations = listOf("foundry.veil:veil-common-1.21.1"),
                projectPath = ":common",
                shouldUnpack = false,
            ),
        )
    )
    addMod(
        filePath = "./mods/MapAtlases",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("maven.modrinth:mapatlases"),
                projectPath = ":neoforge",
                shouldUnpack = true
            ),
            modProject(
                dependencyNotations = listOf("maven.modrinth:mapatlases-common"),
                projectPath = ":common",
                shouldUnpack = false
            )
        )
    )
    addMod(
        filePath = "./mods/create_power_loader",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.hlysine.create_power_loader:create_power_loader"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/sable-companion",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("dev.ryanhcode.sable-companion:sable-companion-common-1.21.1"),
                projectPath = ":sable-companion-common",
                shouldUnpack = true
            ),
            modProject(
                dependencyNotations = listOf("dev.ryanhcode.sable-companion:sable-companion-fabric-1.21.1"),
                projectPath = ":sable-companion-fabric",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/ironchest",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.progwml6.ironchest:ironchest"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/ironshulkerboxes",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.progwml6.ironshulkerboxes:ironshulkerbox"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/Create-Bits-n-Bobs",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.kipti.bnb:bits_n_bobs"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/CreateCentralKitchen",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("plus.dragons.createcentralkitchen:create_central_kitchen"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/FunctionalStorage",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.buuz135:functionalstorage"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/Neo-Language-Reload",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.euphony:neo_language_reload"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/CreateBigCannons",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("com.rbasamoyai:createbigcannons"),
                projectPath = ":",
                shouldUnpack = true
            ),
        )
    )
    addMod(
        filePath = "./mods/create-propulsion-simulated",
        includeTransitive = false,
        modProjects = listOf(
            modProject(
                dependencyNotations = listOf("dev.propulsionteam.propulsionsimulated:createpropulsion"),
                projectPath = ":",
                shouldUnpack = true
            )
        )
    )

    includeExtra("extra_mods.txt")
}
