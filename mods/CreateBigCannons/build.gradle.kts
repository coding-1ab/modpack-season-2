import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
	java
	`maven-publish`
	id("architectury-plugin") version "3.4-SNAPSHOT"
	id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
}

architectury {
	minecraft = rootProject.extra["minecraft_version"] as String
}

val ci = System.getenv("CI")?.toBoolean() ?: false
val release = System.getenv("RELEASE")?.toBoolean() ?: false
val nightly = ci && !release

allprojects {
	apply(plugin = "java")
	apply(plugin = "architectury-plugin")

	base.archivesName.set(rootProject.extra["archives_base_name"] as String)
	group = rootProject.group as String

	repositories {
		// Add repositories here if needed.
	}

	tasks.withType(JavaCompile::class).configureEach {
		options.encoding = "UTF-8"
	}

	java {
		withSourcesJar()
	}
}


subprojects {
	apply(plugin = "dev.architectury.loom")
	apply(plugin = "maven-publish")

	val loom = project.extensions.getByType<LoomGradleExtensionAPI>()
	loom.apply {
		silentMojangMappingsLicense()
	}
	repositories {
		mavenCentral()
		maven { url = uri("https://maven.shedaniel.me/") }
		maven { url = uri("https://maven.blamejared.com/") }
		maven { url = uri("https://maven.parchmentmc.org") }
		maven { url = uri("https://maven.quiltmc.org/repository/release") }
		maven {
			url = uri("https://maven.tterrag.com/")
			content {
				includeGroup("com.jozufozu.flywheel")
			}
		}
		maven { url = uri("https://cursemaven.com") }
		exclusiveContent {
			forRepository {
				maven {
					name = "Modrinth"
					url = uri("https://api.modrinth.com/maven")
				}
			}
			filter {
				includeGroup("maven.modrinth")
			}
		}
	}

	dependencies {
		"minecraft"("com.mojang:minecraft:${rootProject.extra["minecraft_version"]}")
		"mappings"(loom.layered {
			mappings("org.quiltmc:quilt-mappings:${rootProject.extra["minecraft_version"]}+build.${rootProject.extra["qm_version"]}:intermediary-v2")
			parchment("org.parchmentmc.data:parchment-${rootProject.extra["minecraft_version"]}:${rootProject.extra["parchment_version"]}@zip")
			officialMojangMappings { nameSyntheticMembers = false }
		})
	}

	val buildNumber = System.getenv("GITHUB_RUN_NUMBER")
	version = "${rootProject.extra["mod_version"]}${if (release) "" else "-dev"}+mc.${rootProject.extra["minecraft_version"]}-${project.name}${if (nightly) "-build.${buildNumber}" else ""}"

	publishing {
		publications {
			create<MavenPublication>("mavenJava") {
				artifactId = rootProject.extra["archives_base_name"] as String
				from(components["java"])
			}
		}
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/cannoneers-of-create/createbigcannons")
				credentials {
					username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
					password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
				}
			}
			maven {
				name = "realRobotixMaven"
				url = uri("https://maven.realrobotix.me/createbigcannons")
				credentials(PasswordCredentials::class)
			}
		}
	}
}
