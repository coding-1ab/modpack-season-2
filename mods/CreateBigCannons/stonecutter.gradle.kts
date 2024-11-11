import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
	id("dev.kikugie.stonecutter")
	id("dev.architectury.loom") version "1.6-SNAPSHOT" apply false
	id("architectury-plugin") version "3.4-SNAPSHOT" apply false
	id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}
stonecutter active "1.20.1" /* [SC] DO NOT EDIT */
stonecutter.automaticPlatformConstants = true

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
	group = "project"
	ofTask("buildAndCollect")
}

// Builds loader-specific versions into `build/libs/{mod.version}/{loader}`
for (it in stonecutter.tree.branches) {
	if (it.id.isEmpty()) continue
	val loader = it.id.uppercaseFirstChar()
	stonecutter registerChiseled tasks.register("chiseledBuild$loader", stonecutter.chiseled) {
		group = "project"
		versions { branch, _ -> branch == it.id }
		ofTask("buildAndCollect")
	}
}

// Runs active versions for each loader
for (it in stonecutter.tree.nodes) {
	if (it.metadata != stonecutter.current || it.branch.id.isEmpty()) continue
	val types = listOf("Client", "Server")
	val loader = it.branch.id.uppercaseFirstChar()
	for (type in types) it.tasks.register("runActive$type$loader") {
		group = "project"
		dependsOn("run$type")
	}
}

subprojects {
	repositories {
		mavenCentral()
		// mappings
		maven("https://maven.parchmentmc.org")
		maven("https://maven.quiltmc.org/repository/release")
		// our repo
		maven("https://maven.realrobotix.me/master/")

		//maven("https://maven.shedaniel.me/")
		maven("https://maven.blamejared.com/")
		maven("https://maven.tterrag.com/")
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
		exclusiveContent {
			forRepository {
				maven {
					name = "CurseMaven"
					url = uri("https://cursemaven.com")
				}
			}
			filter {
				includeGroup("curse.maven")
			}
		}
		flatDir{
			dir("libs")
		}
	}
}
