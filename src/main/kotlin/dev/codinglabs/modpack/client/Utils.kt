package dev.codinglabs.modpack.client

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player

fun getLocalPlayer(): Player? {
    return Minecraft.getInstance().player
}
