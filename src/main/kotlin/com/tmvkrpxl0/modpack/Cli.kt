package com.tmvkrpxl0.modpack

import com.mojang.serialization.JsonOps
import com.tmvkrpxl0.modpack.updator.Registry
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.writeText

fun main() {
    val path = Path("./createdModJars")
    if (path.notExists()) {
        throw RuntimeException("Run :createModJars first!")
    }

    val files = path.toFile().listFiles()
    val sources = files.filter { it.extension == "assetSource" }
    val mods = files.filter { it.extension == "jar" }
    val modEntries = mutableListOf<Registry.ModEntry>()

    for(mod in mods) {
        val assetSourceFile = sources
            .filter { mod.nameWithoutExtension.startsWith(it.nameWithoutExtension) }
            .maxByOrNull { it.name.length }
        val assetSource = if (assetSourceFile != null) {
            val contents = assetSourceFile.readText(StandardCharsets.UTF_8)
            val tokens = contents.split(" ")

            when(tokens[0]) {
                "curseforge" -> {
                    val projectId = tokens[1].toLong()
                    val fileId = tokens[2].toLong()
                    Registry.ModSource.CurseForge(projectId, fileId)
                }
                "modrinth" -> {
                    val version = tokens[1]
                    Registry.ModSource.Modrinth(version)
                }
                else -> {
                    throw RuntimeException("Invalid file format!")
                }
            }
        } else {
            Registry.ModSource.Inline.INSTANCE
        }

        val crc = CRC32()
        val fileContents = mod.readBytes()
        crc.update(fileContents)
        val checksum = crc.value

        modEntries.add(Registry.ModEntry(mod.name, checksum, assetSource))
    }

    val extraMods = mutableListOf<Registry.ModSource.ExternalSource>()
    extraMods.add(Registry.ModSource.Modrinth("ILW6vM7o")) // guideme
    extraMods.add(Registry.ModSource.Modrinth("6e8GCrLb")) // terra blender
    extraMods.add(Registry.ModSource.Modrinth("EgWWSAhJ")) // puzzles lib
    // TODO Implement CurseForge download
    // extraMods.add(Registry.ExternalSource.CurseForge(457570, 7276577)) // configured
    extraMods.add(Registry.ModSource.Modrinth("OBp8ltOS")) // easy shulker boxes
    extraMods.add(Registry.ModSource.Modrinth("WYcVbsBw")) // configurable
    extraMods.add(Registry.ModSource.Modrinth("FaNppCJJ")) // controlling
    extraMods.add(Registry.ModSource.Modrinth("iEE85X0w")) // searchables
    extraMods.add(Registry.ModSource.Modrinth("yd8FKCmx")) // jade
    extraMods.add(Registry.ModSource.Modrinth("6U8JVjdw")) // modernfix
    extraMods.add(Registry.ModSource.Modrinth("yohfFbgD")) // curios
    extraMods.add(Registry.ModSource.Modrinth("JZIT5IeN")) // moonlight lib
    extraMods.add(Registry.ModSource.Modrinth("x7kQWVju")) // ferrite core
    extraMods.add(Registry.ModSource.Modrinth("YAcQ6elZ")) // jei
    extraMods.add(Registry.ModSource.Modrinth("ouSj7NfF")) // emi


    val registry = Registry(modEntries, extraMods)
    val json = Registry.CODEC.encodeStart(JsonOps.INSTANCE, registry).orThrow
    path.resolve("registry.json").writeText(json.toString())
}
