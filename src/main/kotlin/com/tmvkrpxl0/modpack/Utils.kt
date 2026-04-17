package org.example.com.tmvkrpxl0.modpack

import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Fox
import net.minecraft.world.level.gameevent.GameEvent

fun randomTeleport(entity: LivingEntity, level: ServerLevel) {
    val random = entity.random

    repeat(16) {
        val x = entity.x + (random.nextDouble() - 0.5) * 16.0
        val y = Mth.clamp(
            entity.y + (random.nextInt(16) - 8).toDouble(),
            level.minBuildHeight.toDouble(),
            (level.minBuildHeight + level.logicalHeight - 1).toDouble()
        )
        val z = entity.z + (entity.getRandom().nextDouble() - 0.5) * 16.0
        if (entity.isPassenger) {
            entity.stopRiding()
        }
        val originalPosition = entity.position()

        if (entity.randomTeleport(x, y, z, true)) {
            level.gameEvent(GameEvent.TELEPORT, originalPosition, GameEvent.Context.of(entity))
            val soundSource: SoundSource
            val soundEvent: SoundEvent
            if (entity is Fox) {
                soundEvent = SoundEvents.FOX_TELEPORT
                soundSource = SoundSource.NEUTRAL
            } else {
                soundEvent = SoundEvents.CHORUS_FRUIT_TELEPORT
                soundSource = SoundSource.PLAYERS
            }

            level.playSound(
                null,
                entity.x,
                entity.y,
                entity.z,
                soundEvent,
                soundSource
            )
            entity.resetFallDistance()
            return
        }
    }

}
