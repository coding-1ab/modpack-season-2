package com.tmvkrpxl0.modpack.client

import net.minecraft.client.Minecraft
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.ViewportEvent
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
import com.tmvkrpxl0.modpack.Fluids

fun registerClientExtensions(event: RegisterClientExtensionsEvent) {
    event.registerFluidType(EnderFuelExtension, Fluids.ENDER_FUEL_TYPE)
}

fun modifyFov(event: ViewportEvent.ComputeFov) {
    val minecraft = Minecraft.getInstance() ?: return
    if (event.camera.entity.isEyeInFluidType(Fluids.ENDER_FUEL_TYPE)) {
        event.fov *= Mth.lerp(minecraft.options.fovEffectScale().get(), 1.0, 0.85714287)
    }
}

private var wasInEnderFuel = false

fun playWaterAmbient(@Suppress("unused") event: ClientTickEvent.Pre) {
    val minecraft = Minecraft.getInstance() ?: return
    val player = minecraft.player ?: return

    val isNowInFuel = player.isEyeInFluidType(Fluids.ENDER_FUEL_TYPE)
    if (wasInEnderFuel != isNowInFuel) {
        val soundEvent = if (!wasInEnderFuel) {
            SoundEvents.AMBIENT_UNDERWATER_ENTER
        } else {
            SoundEvents.AMBIENT_UNDERWATER_EXIT
        }

        player.level().playLocalSound(
            player.x,
            player.y,
            player.z,
            soundEvent,
            SoundSource.AMBIENT,
            1.0F,
            1.0F,
            false
        )

        wasInEnderFuel = isNowInFuel
    }
}
