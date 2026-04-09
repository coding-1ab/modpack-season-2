import org.gradle.kotlin.dsl.neoForge
import org.slf4j.event.Level

plugins {
    kotlin("jvm") version "2.3.0"
    id("java-library")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.141"
    id("idea")
}

group = "com.tmvkrpxl0"
version = "1.0-SNAPSHOT"

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
            gameDirectory.dir("run/clinet")
            devLogin = true
        }
        create("client") {
            client()
            gameDirectory.dir("run/clinet")
            // systemProperty("neoforge.enabledGameTestNamespaces", project.mod_id)
        }

        create("server") {
            server()
            gameDirectory.dir("run/server")
            programArgument("--nogui")
            // systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        create("gameTestServer") {
            type = "gameTestServer"
            // systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }

        create("data") {
            data()

            // example of overriding the workingDirectory set in configureEach above, uncomment if you want to use it
            // gameDirectory = project.file('run-data')

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            // programArguments.addAll '--mod', project.mod_id, '--all', '--output', file('src/generated/resources/').getAbsolutePath(), '--existing', file('src/main/resources/').getAbsolutePath()
        }

        // applies to all the run configs above
        configureEach {
            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            systemProperty("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            logLevel = Level.DEBUG
            jvmArguments.addAll(
                "-XX:+IgnoreUnrecognizedVMOptions",
                "-XX:+AllowEnhancedClassRedefinition",
                //jvmArgument("-XX:-OmitStackTraceInFastThrow", // uncomment when you get exceptions with null messages etc
                // "-XX:+UnlockCommercialFeatures", // uncomment for profiling
            )

            systemProperty("mixin.debug.verbose", "true")
            systemProperty("mixin.debug.export", "true")
            loggingConfigFile = project.file("log4j.xml")
        }
    }

    /*mods {
        // define mod <-> source bindings
        // these are used to tell the game which sources are for which mod
        // multi mod projects should define one per mod
        "$mod_id" {
            sourceSet(sourceSets.main)
        }
    }*/
}

repositories {
    mavenCentral()
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven("https://maven.createmod.net") // Create, Ponder, Flywheel
    maven("https://maven.ithundxr.dev/snapshots") // Registrate
    maven {
        // location of the maven that hosts JEI files since January 2023
        name = "Jared's maven"
        url = uri("https://maven.blamejared.com/")
    }
    maven {
        // location of a maven mirror for JEI files, as a fallback
        name = "ModMaven"
        url = uri("https://modmaven.dev")
    }

    maven("https://maven.realrobotix.me/copycats") {
        // maven for Copycats+
        name = "realrobotixCopycats"
    }
    maven("https://maven.fallenbreath.me/releases") {
        // Conditional Mixin
        name = "ConditionalMixin"
    }
    maven("https://maven.dragons.plus/releases") {
        // Create: Dragons Plus
        name = "DragonsPlusMinecraft"
    }
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://cursemaven.com") {
                name = "Curse Maven"
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }
    maven("https://maven.theillusivec4.top/") // Curios API
    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }
}

val localRuntime = configurations.maybeCreate("localRuntime")
configurations {
    runtimeClasspath {
        extendsFrom(localRuntime)
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("thedarkcolour:kotlinforforge-neoforge:5.+")

    localRuntime("com.simibubi.create:create-${property("minecraft_version")}:${property("create_version")}:slim") { isTransitive = false }
    localRuntime("net.createmod.ponder:ponder-neoforge:${property("ponder_version")}+mc${property("minecraft_version")}")
    localRuntime("dev.engine-room.flywheel:flywheel-neoforge-${property("minecraft_version")}:${property("flywheel_version")}")
    localRuntime("com.tterrag.registrate:Registrate:${property("registrate_version")}")
    localRuntime("com.hlysine.create_connected:create_connected") {
        isTransitive = false
    }
    localRuntime("com.mrh0.createaddition:createaddition") {
        isTransitive = false
    }
    localRuntime("squeek.appleskin:appleskin")
    localRuntime("org.appliedenergistics:appliedenergistics2")
    /*localRuntime("org.antarcticgardens.cna:create-new-age") {
        isTransitive = false
    }*/
    localRuntime("mezz.jei:jei-${property("minecraft_version")}-neoforge")

    add("additionalRuntimeClasspath", "org.jetbrains:annotations")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    test {
        useJUnitPlatform()
    }

    named<Wrapper>("wrapper").configure {
        distributionType = Wrapper.DistributionType.BIN
    }
}

sourceSets.main.get().resources {
    // Include resources generated by data generators.
    srcDir("src/generated/resources")

    // Exclude common development only resources from finalized outputs
    exclude("**/*.bbmodel") // BlockBench project files
    exclude("src/generated/**/.cache") // datagen cache files
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}
