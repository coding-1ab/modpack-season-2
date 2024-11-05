import java.util.*

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath

    runs {
        create("data") {
            client()

			name("Fabric Data Generation")
			vmArg("-Dfabric-api.datagen")
			vmArg("-Dfabric-api.datagen.output-dir=${project.rootProject.file("fabric/src/generated/resources")}")
			vmArg("-Dfabric-api.datagen.modid=createbigcannons")
			vmArg("-Dporting_lib.datagen.existing_resources=${project.rootProject.file("common/src/main/resources")}")
			vmArg("-Dcreatebigcannons.datagen.platform=fabric")
		}
		create("data1") {
			client()

			name("Forge Data Generation (Fabric)")
			vmArg("-Dfabric-api.datagen")
			vmArg("-Dfabric-api.datagen.output-dir=${project.rootProject.file("forge/src/generated/resources")}")
			vmArg("-Dfabric-api.datagen.modid=createbigcannons")
			vmArg("-Dporting_lib.datagen.existing_resources=${project.rootProject.file("common/src/main/resources")}")
			vmArg("-Dcreatebigcannons.datagen.platform=forge")
		}
	}
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentFabric: Configuration by configurations.getting

configurations {
	compileOnly.configure { extendsFrom(common) }
	runtimeOnly.configure { extendsFrom(common) }
	developmentFabric.extendsFrom(common)
}

configurations.configureEach {
	resolutionStrategy {
        force("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
	}
}

repositories {
    // mavens for Fabric-exclusives
    maven { url = uri("https://mvn.devos.one/#/") }
    maven { url = uri("https://api.modrinth.com/maven") } // LazyDFU
    maven { url = uri("https://maven.terraformersmc.com/releases/") } // Mod Menu, EMI, Trinkets
    maven { url = uri("https://mvn.devos.one/snapshots/") }
    // Create Fabric, Forge Tags, Milk Lib, Registrate Fabric
    maven { url = uri("https://mvn.devos.one/releases") } // Porting Lib Releases
    maven { url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") } // Forge config api port
    maven { url = uri("https://maven.cafeteria.dev/releases") } // Fake Player API
    maven { url = uri("https://maven.jamieswhiteshirt.com/libs-release") } // Reach Entity Attributes
    maven { url = uri("https://jitpack.io/") } // Mixin Extras, Fabric ASM
    maven { url = uri("https://maven.ladysnake.org/releases") } // Trinkets
    maven {
        url = uri("https://maven.realrobotix.me/master/")
		content {
			includeGroup("com.rbasamoyai")
		}
	}
	flatDir {
        dir("libs")
	}
}


dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) { isTransitive = false }

    // dependencies
	modApi("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")

    // Create - dependencies are added transitively
    modImplementation("com.simibubi.create:create-fabric-${rootProject.property("minecraft_version")}:${rootProject.property("create_fabric_version")}")

    // Development QOL
    modLocalRuntime("maven.modrinth:lazydfu:${rootProject.property("lazydfu_version")}")
    modImplementation("com.terraformersmc:modmenu:${rootProject.property("modmenu_version")}")

    // Recipe Viewers - Create Fabric supports JEI, REI, and EMI.
    // See root gradle.properties to choose which to use at runtime.
    when (rootProject.property("fabric_recipe_viewer").toString().lowercase(Locale.ROOT)) {
        "jei" -> modLocalRuntime("mezz.jei:jei-${rootProject.property("minecraft_version")}-fabric:${rootProject.property("jei_version")}")
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:${rootProject.property("rei_version")}")
        "emi" -> modLocalRuntime("dev.emi:emi-fabric:${rootProject.property("emi_version")}")
        "disabled" -> {}
        else -> println("Unknown recipe viewer specified: ${rootProject.property("fabric_recipe_viewer")}. Must be JEI, REI, EMI, or disabled.")
    }
    // if you would like to add integration with them, uncomment them here.
	modCompileOnly("mezz.jei:jei-${rootProject.property("minecraft_version")}-fabric:${rootProject.property("jei_version")}") { isTransitive = false }
	modCompileOnly("mezz.jei:jei-${rootProject.property("minecraft_version")}-common:${rootProject.property("jei_version")}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${rootProject.property("rei_version")}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:${rootProject.property("rei_version")}")
    modCompileOnly("dev.emi:emi-fabric:${rootProject.property("emi_version")}")

    modLocalRuntime("curse.maven:spark-361579:${rootProject.property("spark_fabric_file")}") // Spark

	// Ritchie's Projectile Library
	val rplSuffix = if (rootProject.property("use_local_rpl_build").toString().toBoolean()) "" else "-build.${rootProject.property("rpl_build")}"
	modImplementation(include("com.rbasamoyai:ritchiesprojectilelib:${rootProject.property("rpl_version")}+mc.${rootProject.property("minecraft_version")}-fabric" + rplSuffix) { isTransitive = false })

	// Fixes, integration
	//modImplementation("curse.maven:free-cam-557076:${freecam_fabric_file}") // Freecam
	modImplementation("curse.maven:copycats-968398:${rootProject.property("copycats_fabric_file")}")
	// Trinkets and CCA
	modLocalRuntime("dev.emi:trinkets:${rootProject.property("trinkets_fabric_version")}")
	modCompileOnly("dev.emi:trinkets:${rootProject.property("trinkets_fabric_version")}") { exclude(group = "com.terraformersmc") }
	modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${rootProject.property("cca_fabric_version")}")
	modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${rootProject.property("cca_fabric_version")}")
}

tasks.named<ProcessResources>("processResources") {
    // set up properties for filling into metadata
    val properties = mapOf(
        "version" to version,
        "fabric_loader_version" to rootProject.property("fabric_loader_version"),
        "fabric_api_version" to rootProject.property("fabric_api_version"),
        "minecraft_version" to rootProject.property("minecraft_version"),
        "create_version" to rootProject.property("create_fabric_version"), // on fabric, use the entire version, unlike forge
        "copycats_breaks" to rootProject.property("copycats_breaks_fabric"),
        "trinkets_breaks" to rootProject.property("trinkets_breaks_fabric")
    )
    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}


tasks.shadowJar {
	exclude("architectury.common.json")
	configurations = listOf(shadowCommon)
	archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
	injectAccessWidener.set(true)
	inputFile.set(tasks.shadowJar.get().archiveFile)
	dependsOn(tasks.shadowJar)
	archiveClassifier.set(null as String?)
}

tasks.jar {
	archiveClassifier.set("dev")
}

tasks.sourcesJar {
	val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
	dependsOn(commonSources)
	from(commonSources.archiveFile.map { zipTree(it) })
}

components.getByName("java") {
	this as AdhocComponentWithVariants
	this.withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
		skip()
	}
}

sourceSets["main"].resources {
    // include generated resources in resources
        srcDir("src/generated/resources")
        exclude("src/generated/resources/.cache")
}
