plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("addMods") {
            id = "com.tmvkrpxl0.gradle.add-mod"
            implementationClass = "com.tmvkrpxl0.gradle.AddMod"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.neoforged.net") {
        name = "NeoForge"
        content {
            includeGroup("net.neoforged")
        }
    }
}

dependencies {
    implementation("net.neoforged.moddev:net.neoforged.moddev.gradle.plugin:2.0.141")
}

kotlin {
    jvmToolchain(21)
}
