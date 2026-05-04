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

base {
    archivesName.set(mod_id)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

neoForge {
    version = neo_version

    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

// 데이터 제너레이터로 생성된 리소스 포함
sourceSets.main {
    resources {
        srcDir("src/generated/resources")
    }
}

dependencies {
    // 필요한 의존성을 여기에 추가하세요.
}

// 모드 메타데이터 생성 태스크 (neoforge.mods.toml 등)
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    description = "Generates mod metadata files (e.g., neoforge.mods.toml) by processing templates with project properties."
    val replaceProperties = mapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "neo_version" to neo_version,
        "neo_version_range" to neo_version_range,
        "loader_version_range" to loader_version_range,
        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_version" to mod_version,
        "mod_authors" to mod_authors,
        "mod_description" to mod_description
    )

    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main.get().resources.srcDir(generateModMetadata)


neoForge.ideSyncTask(generateModMetadata)

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
