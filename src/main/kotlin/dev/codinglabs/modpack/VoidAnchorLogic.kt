package dev.codinglabs.modpack

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.RelativeMovement
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

object VoidAnchorLogic {
    fun refreshActivation(level: Level, pos: BlockPos, state: BlockState): BlockState {
        val active = if (level.dimension() == Level.OVERWORLD) {
            hasNearbyEndPortal(level as ServerLevel, pos)
        } else {
            false
        }

        if (state.getValue(VoidAnchorBlock.ACTIVE) != active) {
            val updated = state.setValue(VoidAnchorBlock.ACTIVE, active)
            level.setBlock(pos, updated, 3)
            return updated
        }

        return state
    }

    fun handleVoidDeathRescue(player: ServerPlayer, level: ServerLevel, source: DamageSource): Boolean {
        if (level.dimension() != Level.END) {
            return false
        }
        if (!source.`is`(DamageTypes.FELL_OUT_OF_WORLD)) {
            // 엔드 공허 낙사만 구조
            return false
        }
        val anchor = player.voidAnchorPos ?: return false
        rescueToAnchor(player, anchor)

        return true
    }

    private fun rescueToAnchor(player: ServerPlayer, anchorPos: BlockPos): Boolean {
        val overworld = player.server.getLevel(Level.OVERWORLD) ?: return false
        val anchorState = overworld.getBlockState(anchorPos)

        val newState = refreshActivation(overworld, anchorPos, anchorState)
        if (!newState.getValue(VoidAnchorBlock.ACTIVE)) {
            return false
        }

        val charges = newState.getValue(VoidAnchorBlock.CHARGES)
        if (charges <= 0) {
            return false
        }

        val targetPos = findSafeTeleportPos(overworld, anchorPos) ?: return false
        val consumed = newState.setValue(VoidAnchorBlock.CHARGES, charges - 1)
        overworld.setBlock(anchorPos, consumed, 3)

        teleportAndRevive(player, overworld, targetPos.center)
        return true
    }

    private fun teleportAndRevive(player: ServerPlayer, targetLevel: ServerLevel, targetPos: Vec3) {
        player.teleportTo(
            targetLevel,
            targetPos.x,
            targetPos.y,
            targetPos.z,
            mutableSetOf<RelativeMovement>(),
            player.yRot,
            player.xRot,
        )
        player.health = 1.0f
        player.fallDistance = 0.0f
        player.invulnerableTime = 20
    }

    private fun findSafeTeleportPos(level: ServerLevel, anchorPos: BlockPos): BlockPos? {
        val offsets = listOf(
            BlockPos.ZERO,
            BlockPos(1, 0, 0), BlockPos(-1, 0, 0),
            BlockPos(0, 0, 1), BlockPos(0, 0, -1),
            BlockPos(1, 0, 1), BlockPos(1, 0, -1),
            BlockPos(-1, 0, 1), BlockPos(-1, 0, -1),
        )

        for (offset in offsets) {
            val base = anchorPos.offset(offset)
            val feet = base.above()
            val head = feet.above()
            val below = base.below()
            val feetState = level.getBlockState(feet)
            val headState = level.getBlockState(head)
            val belowState = level.getBlockState(below)

            if (feetState.isAir && headState.isAir && belowState.isFaceSturdy(level, below, Direction.UP)) {
                return feet
            }
        }

        return null
    }

    private fun hasNearbyEndPortal(level: ServerLevel, pos: BlockPos): Boolean {
        val chunkPos = level.getChunkAt(pos).pos

        for (x in -1..1) {
            for (z in -1..1) {
                val chunk = level.getChunk(chunkPos.x + x, chunkPos.z + z)
                chunk.blockEntities.forEach { (_, entity) ->
                    if (isEndPortalBlock(entity.blockState)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isEndPortalBlock(state: BlockState): Boolean {
        return state.`is`(Blocks.END_PORTAL)
    }
}
