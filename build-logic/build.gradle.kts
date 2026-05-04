plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("addMods") {
            id = "dev.codinglabs.gradle.add-mod"
            implementationClass = "dev.codinglabs.gradle.AddMod"
        }
    }
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}
