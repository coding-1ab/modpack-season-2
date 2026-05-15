package dev.codinglabs.modpack.client

import dev.codinglabs.modpack.ID
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(ID, dist = [Dist.CLIENT])
object ModPackTweaksClient {
    init {
        MOD_BUS.addListener(::registerClientExtensions)
        MOD_BUS.addListener(::registerBlockEntityRenderers)
        FORGE_BUS.addListener(::modifyFov)
        FORGE_BUS.addListener(::playWaterAmbient)
    }
}
