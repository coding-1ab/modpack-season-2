package org.example.com.tmvkrpxl0.modpack

import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(ModPackTweaks.ID)
object ModPackTweaks {
    const val ID = "modpack_tweaks"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.info("Starting Coding Lab Modpack Tweaks")
    }
}
