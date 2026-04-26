package com.tmvkrpxl0.modpack

import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.tmvkrpxl0.modpack.data.gatherData
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(ModPackTweaks.ID)
object ModPackTweaks {
    const val ID = "modpack_tweaks"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.info("Starting Coding Lab Modpack Tweaks")

        Blocks.register(MOD_BUS)
        Items.register(MOD_BUS)
        Fluids.register(MOD_BUS)
        MOD_BUS.addListener(::gatherData)
    }
}

fun String.toResource() = ResourceLocation.fromNamespaceAndPath(ModPackTweaks.ID, this)
