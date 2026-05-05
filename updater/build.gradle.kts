import org.slf4j.event.Level

plugins {
    id("java-library")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.141"
    id("idea")
}

val mod_id: String by project
val mod_version: String by project
val mod_group_id: String by project
val neo_version: String by project
val minecraft_version: String by project
val minecraft_version_range: String by project
val neo_version_range: String by project
val loader_version_range: String by project
val mod_name: String by project
val mod_license: String by project
val mod_authors: String by project
val mod_description: String by project

version = mod_version
group = mod_group_id

repositories {
    mavenLocal()
    mavenCentral()
}

neoForge {
    // Specify the version of NeoForge to use.
    version = project.properties["neo_version"]!! as String

    parchment {
        mappingsVersion = project.properties["parchment_mappings_version"]!! as String
        minecraftVersion = project.properties["parchment_minecraft_version"]!! as String
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
        }
    }
}

base {
    archivesName.set(mod_id)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// 데이터 제너레이터로 생성된 리소스 포함
sourceSets.main {
    resources {
        srcDir("src/generated/resources")
    }
}

dependencies {
    jarJar(create("dev.codinglabs:dev.codinglabs"))
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
