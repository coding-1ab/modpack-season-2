plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
	platformSetupLoomIde()
	forge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

	forge.apply {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
	}
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentForge: Configuration by configurations.getting

configurations {
	compileOnly.configure { extendsFrom(common) }
	runtimeOnly.configure { extendsFrom(common) }
	developmentForge.extendsFrom(common)
}

repositories {
    // mavens for Forge-exclusives
    maven("https://maven.theillusivec4.top/") // Curios
    maven("https://maven.tterrag.com/") {
        content {
            includeGroup("com.tterrag.registrate")
            includeGroup("com.simibubi.create")
        }
    }
    maven("https://maven.realrobotix.me/master/") { // Ritchie's Projectile Library
		content {
			includeGroup("com.rbasamoyai")
		}
	}
	flatDir {
        dir("./libs")
	}
    mavenCentral()
}

dependencies {
    forge("net.minecraftforge:forge:${rootProject.property("minecraft_version")}-${rootProject.property("forge_version")}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionForge")) { isTransitive = false }

	// Create and its dependencies
    modImplementation("com.simibubi.create:create-${rootProject.property("minecraft_version")}:${rootProject.property("create_forge_version")}:slim") { isTransitive = false }
    modImplementation("com.tterrag.registrate:Registrate:${rootProject.property("registrate_forge_version")}")
	// Please replace 1.20 with ${minecraft_version} at a later date
    modImplementation("com.jozufozu.flywheel:flywheel-forge-${rootProject.property("minecraft_version")}:${rootProject.property("flywheel_forge_version")}")

	// Development QOL
    modLocalRuntime("mezz.jei:jei-${rootProject.property("minecraft_version")}-forge:${rootProject.property("jei_version")}") { isTransitive = false }

	// if you would like to add integration with JEI, uncomment this line.
    modCompileOnly("mezz.jei:jei-${rootProject.property("minecraft_version")}-forge-api:${rootProject.property("jei_version")}")

    modImplementation("curse.maven:spark-361579:${rootProject.property("spark_forge_file")}") // Spark

	// Ritchie's Projectile Library
    val rplSuffix = if (rootProject.property("use_local_rpl_build").toString().toBoolean()) "" else "-build.${rootProject.property("rpl_build")}"
    modImplementation(include("com.rbasamoyai:ritchiesprojectilelib:${rootProject.property("rpl_version")}+mc.${rootProject.property("minecraft_version")}-forge$rplSuffix"){ isTransitive = false })
	// Create: Unify
    modImplementation("maven.modrinth:create-unify:${rootProject.property("unify_forge_file")}")

    compileOnly("io.github.llamalad7:mixinextras-common:${rootProject.property("mixinextras_version")}")
    annotationProcessor(include("io.github.llamalad7:mixinextras-forge:${rootProject.property("mixinextras_version")}"){})

	// Fixes, integration
    // "modImplementation"("curse.maven:free-cam-557076:${rootProject.property("freecam_forge_file")}") // Freecam
    modImplementation("curse.maven:copycats-968398:${rootProject.property("copycats_forge_file")}")
    modImplementation("curse.maven:framedblocks-441647:${rootProject.property("framedblocks_forge_file")}")
    forgeRuntimeLibrary("com.github.ben-manes.caffeine:caffeine:3.1.1") // For FramedBlocks

	// Curios
    modRuntimeOnly("top.theillusivec4.curios:curios-forge:${rootProject.property("curios_forge_version")}")
    modCompileOnly("top.theillusivec4.curios:curios-forge:${rootProject.property("curios_forge_version")}:api")
}

tasks.processResources {
    // set up properties for filling into metadata
    val properties = mapOf(
        "version" to version as String,
        "forge_version" to rootProject.property("forge_version").toString().split(".")[0], // only specify major version of forge
        "minecraft_version" to rootProject.property("minecraft_version"),
        "create_version" to rootProject.property("create_forge_version").toString().substringBefore("-"),
        "unify_version" to rootProject.property("unify_forge_version"),
        "copycats_requirement" to rootProject.property("copycats_requirement_forge"),
        "framedblocks_requirement" to rootProject.property("framedblocks_requirement_forge"),
        "curios_requirement" to rootProject.property("curios_requirement_forge")
    )
    properties.forEach { (k, v) -> inputs.property(k, v) }

	filesMatching("META-INF/mods.toml") {
        expand(properties)
	}
}

loom {
	forge {
		mixinConfig(
			"createbigcannons-common.mixins.json",
			"createbigcannons.mixins.json"
		)
	}
}

tasks.shadowJar {
	exclude("fabric.mod.json")
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

sourceSets.main {
	resources { // include generated resources in resources
		srcDir("src/generated/resources")
		exclude("src/generated/resources/.cache")
	}
}
