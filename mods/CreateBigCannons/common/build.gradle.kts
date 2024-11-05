architectury {
    common(rootProject.extra["enabled_platforms"].toString().split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/createbigcannons.accesswidener"))
}

repositories {
    // mavens for Create Fabric and dependencies
    maven { url = uri("https://mvn.devos.one/#/") }
    maven { url = uri("https://api.modrinth.com/maven") } // LazyDFU
    maven { url = uri("https://maven.terraformersmc.com/releases/") } // Mod Menu
    maven { url = uri("https://mvn.devos.one/snapshots/") }
    // Create Fabric, Forge Tags, Milk Lib, Registrate Fabric
    maven { url = uri("https://mvn.devos.one/releases") } // Porting Lib Releases
    maven { url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") } // Forge config api port
    maven { url = uri("https://maven.cafeteria.dev/releases") } // Fake Player API
    maven { url = uri("https://maven.jamieswhiteshirt.com/libs-release") } // Reach Entity Attributes
    maven { url = uri("https://jitpack.io/") } // Mixin Extras, Fabric ASM
    maven {
        // Ritchie's Projectile Library
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
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.extra["fabric_loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.extra["fabric_api_version"]}")
    // Compile against Create Fabric in common
    // beware of differences across platforms!
    // dependencies must also be pulled in to minimize problems, from remapping issues to compile errors.
    // All dependencies except Flywheel and Registrate are NOT safe to use!
    // Flywheel and Registrate must also be used carefully due to differences.
    modCompileOnly("com.simibubi.create:create-fabric-${rootProject.extra["minecraft_version"]}:${rootProject.extra["create_fabric_version"]}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${rootProject.extra["fabric_api_version"]}")

    annotationProcessor("io.github.llamalad7:mixinextras-common:${rootProject.extra["mixinextras_version"]}") // MixinExtras

	// Ritchie's Projectile Library
    val rplSuffix = if (rootProject.extra["use_local_rpl_build"].toString().toBoolean()) "" else "-build.${rootProject.extra["rpl_build"]}"
    modImplementation("com.rbasamoyai:ritchiesprojectilelib:${rootProject.extra["rpl_version"]}+mc.${rootProject.extra["minecraft_version"]}-common$rplSuffix") {
        isTransitive = false
    }

    modImplementation("curse.maven:copycats-968398:${rootProject.extra["copycats_fabric_file"]}")
}
