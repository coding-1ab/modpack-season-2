package org.example.com.tmvkrpxl0.modpack.client

import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import org.example.com.tmvkrpxl0.modpack.ModPackTweaks
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(ModPackTweaks.ID, dist = [Dist.CLIENT])
object ModPackTweaksClient {
    init {
        MOD_BUS.addListener(::registerClientExtensions)
        FORGE_BUS.addListener(::modifyFov)
        FORGE_BUS.addListener(::playWaterAmbient)
    }
}
