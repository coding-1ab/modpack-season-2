import org.slf4j.event.Level

plugins {
    id("java-library")
    id("maven-publish")
    id("net.neoforged.moddev")
    id("idea")
}

val mod_id: String by rootProject
val parchment_mappings_version: String by rootProject
val parchment_minecraft_version: String by rootProject
val mod_group_id: String by rootProject
val neo_version: String by rootProject

repositories {
    mavenLocal()
    mavenCentral()
}

neoForge {
    // Specify the version of NeoForge to use.
    version = neo_version

    parchment {
        mappingsVersion = parchment_mappings_version
        minecraftVersion = parchment_minecraft_version
    }

    // This line is optional. Access Transformers are automatically detected
    // accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        create("clientAuth") {
            client()
            gameDirectory = project.file("runs/client")
            devLogin = true
        }

        create("client") {
            client()
            gameDirectory = project.file("runs/client")
            // systemProperty("neoforge.enabledGameTestNamespaces", project.mod_id)
        }

        create("server") {
            server()
            gameDirectory = project.file("runs/server")
            // systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }

        // applies to all the run configs above
        configureEach {
            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            systemProperty("forge.logging.markers", "REGISTRYDUMP")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            logLevel = Level.DEBUG
            jvmArguments.addAll(
                "-XX:+IgnoreUnrecognizedVMOptions",
                "-XX:+AllowEnhancedClassRedefinition",
                "-XX:+EnableDynamicAgentLoading",
                //jvmArgument("-XX:-OmitStackTraceInFastThrow", // uncomment when you get exceptions with null messages etc
                // "-XX:+UnlockCommercialFeatures", // uncomment for profiling
            )

            loggingConfigFile = rootProject.file("log4j.xml")
        }
    }

    mods {
        // define mod <-> source bindings
        // these are used to tell the game which sources are for which mod
        // multi mod projects should define one per mod
        create(properties["mod_id"]!! as String) {
            sourceSet(sourceSets.main.get())
        }
    }
}

base {
    archivesName.set("modpack-updater")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("${project.projectDir}/repo")
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
