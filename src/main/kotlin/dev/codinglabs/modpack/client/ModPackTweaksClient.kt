package dev.codinglabs.modpack.client

import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import dev.codinglabs.modpack.ModPackTweaks
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
