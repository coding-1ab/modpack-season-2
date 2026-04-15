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
}

kotlin {
    jvmToolchain(21)
}
