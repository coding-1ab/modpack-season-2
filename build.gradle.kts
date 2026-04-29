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

val minecraftVersion = project.properties["minecraft_version"]!! as String
val sodiumVersion = project.properties["sodium_version"]!! as String
val curiosVersion = project.properties["curios_version"]!! as String

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
            jvmArguments.add("-Dmodernfix.allowSparkProfiling=true")
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
            gameDirectory = project.file("runs/data")

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll(
                "--mod", properties["mod_id"]!! as String,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
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

            systemProperty("mixin.debug.verbose", "true")
            systemProperty("mixin.debug.export", "true")
            loggingConfigFile = project.file("log4j.xml")
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

repositories {
    mavenCentral()
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven("https://maven.createmod.net") // Create, Ponder, Flywheel
    maven("https://maven.ithundxr.dev/snapshots") // Registrate
    maven("https://maven.minecraftforge.net/") // Terrablender
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
    maven("https://maven.dragons.plus/releases")
    maven("https://maven.ftb.dev/releases" ) // FTB Mods
    maven("https://maven.architectury.dev" ) // Arch API
    maven("https://jm.gserv.me/repository/maven-public") { // JourneyMap
        content {
            includeGroup("info.journeymap")
            includeGroup("mysticdrew")
        }
    }
    maven("https://maven.idiotss.com/releases/") // ESL

    exclusiveContent { // Sable
        forRepository {
            maven("https://maven.ryanhcode.dev/releases") {
                name = "RyanHCode Maven"
            }
        }
        filter {
            includeGroup("dev.ryanhcode.sable")
            includeGroup("dev.ryanhcode.sable-companion")
        }
    }
}

val localRuntime = configurations.maybeCreate("localRuntime")
configurations {
    runtimeClasspath {
        extendsFrom(localRuntime.get())
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("thedarkcolour:kotlinforforge-neoforge:5.+")

    localRuntime("net.createmod.ponder:ponder-neoforge:${property("ponder_version")}+mc${minecraftVersion}")
    localRuntime("dev.engine-room.flywheel:flywheel-neoforge-${property("minecraft_version")}:${property("flywheel_version")}")
    localRuntime("com.tterrag.registrate:Registrate:${property("registrate_version")}")

    localRuntime("mezz.jei:jei-${minecraftVersion}-neoforge:19.27.0.340")
    localRuntime("dev.emi:emi-neoforge:1.1.22+${minecraftVersion}")
    localRuntime("org.appliedenergistics:guideme:21.1.1")
    localRuntime("com.github.glitchfiend:TerraBlender-neoforge:${minecraftVersion}-4.1.0.8")

    localRuntime("curse.maven:configured-457570:7276577")
    localRuntime("curse.maven:easyshulkerboxes-594006:6697879")
    localRuntime("curse.maven:puzzleslib-495476:7140307")
    localRuntime("curse.maven:configurable-1092048:7356762")
    localRuntime("curse.maven:controlling-250398:6368976")
    localRuntime("curse.maven:searchables-858542:5831692")
    localRuntime("curse.maven:jade-324717:7545219")
    localRuntime("curse.maven:atlas-633577:5490697")
    localRuntime("curse.maven:modernfix-790626:7917721")
    localRuntime("curse.maven:spark-361579:6225208")
    localRuntime("top.theillusivec4.curios:curios-neoforge:$curiosVersion+${minecraftVersion}")
    localRuntime("maven.modrinth:sodium:mc${minecraftVersion}-$sodiumVersion-neoforge")

    compileOnly("com.simibubi.create:create-1.21.1")
    compileOnly("com.mrh0.createaddition:createaddition")
    compileOnly("plus.dragons.createdragonsplus:create-dragons-plus")
    compileOnly("plus.dragons.createenchantmentindustry:create-enchantment-industry")
    compileOnly("org.appliedenergistics:appliedenergistics2")
    implementation("dev.ryanhcode.sable:sable-neoforge-${minecraftVersion}")

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

sourceSets.main {
    resources {
        // Include resources generated by data generators.
        srcDir("src/generated/resources")

        // Exclude common development only resources from finalized outputs
        exclude("**/*.bbmodel") // BlockBench project files
        exclude("src/generated/**/.cache") // datagen cache files
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}

java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
}
