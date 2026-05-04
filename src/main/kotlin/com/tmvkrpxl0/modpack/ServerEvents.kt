package com.tmvkrpxl0.modpack

import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent

fun onLivingDeath(event: LivingDeathEvent) {
    val player = event.entity as? ServerPlayer ?: return
    val level = player.serverLevel()

    if (VoidAnchorLogic.handleVoidDeathRescue(player, level, event.source)) {
        event.isCanceled = true
    }
}

