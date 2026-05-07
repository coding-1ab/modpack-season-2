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

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
}

kotlin {
    jvmToolchain(21)
}
