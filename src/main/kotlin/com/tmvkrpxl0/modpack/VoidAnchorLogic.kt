package com.tmvkrpxl0.modpack

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.RelativeMovement
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

object VoidAnchorLogic {
    private const val MAX_PORTAL_DISTANCE = 25.0
    private const val CLICKED_ANCHOR_TAG = "modpack_tweaks:last_void_anchor"

    fun saveLastClickedAnchor(player: ServerPlayer, pos: BlockPos) {
        player.persistentData.putLong(CLICKED_ANCHOR_TAG, pos.asLong())
    }

    fun refreshActivation(level: Level, pos: BlockPos): BlockState {
        val state = level.getBlockState(pos)
        if (state.block !is VoidAnchorBlock) {
            return state
        }

        val active = if (level.dimension() == Level.OVERWORLD) {
            hasNearbyEndPortal(level, pos)
        } else {
            false
        }

        val blockEntity = level.getBlockEntity(pos) as? VoidAnchorBlockEntity
        if (blockEntity != null) {
            blockEntity.activeByPortal = active
            blockEntity.setChanged()
        }

        if (state.getValue(VoidAnchorBlock.ACTIVE) != active) {
            val updated = state.setValue(VoidAnchorBlock.ACTIVE, active)
            level.setBlock(pos, updated, 3)
            return updated
        }

        return state
    }

    fun handleVoidDeathRescue(player: ServerPlayer, level: ServerLevel, source: net.minecraft.world.damagesource.DamageSource): Boolean {
        if (level.dimension() != Level.END) {
            return false
        }
        if (!source.`is`(DamageTypes.FELL_OUT_OF_WORLD)) {
            // 엔드 공허 낙사만 구조
            return false
        }

        val rescueFromAnchor = findAnchorFromPlayerMemory(player)?.let { anchorPos ->
            rescueToAnchor(player, anchorPos)
        } ?: false

        if (rescueFromAnchor) {
            return true
        }

        return rescueToRespawnPoint(player)
    }

    private fun rescueToAnchor(player: ServerPlayer, anchorPos: BlockPos): Boolean {
        val overworld = player.server.getLevel(Level.OVERWORLD) ?: return false
        val anchorState = refreshActivation(overworld, anchorPos)
        if (anchorState.block != com.tmvkrpxl0.modpack.Blocks.VOID_ANCHOR) {
            return false
        }
        if (!anchorState.getValue(VoidAnchorBlock.ACTIVE)) {
            return false
        }

        val charges = anchorState.getValue(VoidAnchorBlock.CHARGES)
        if (charges <= 0) {
            return false
        }

        val targetPos = findSafeTeleportPos(overworld, anchorPos) ?: return false
        val consumed = anchorState.setValue(VoidAnchorBlock.CHARGES, charges - 1)
        overworld.setBlock(anchorPos, consumed, 3)

        teleportAndRevive(player, overworld, targetPos)
        return true
    }

    private fun rescueToRespawnPoint(player: ServerPlayer): Boolean {
        val server = player.server ?: return false
        val respawnPos = player.respawnPosition ?: return false
        val respawnDimension = player.respawnDimension
        val level = server.getLevel(respawnDimension) ?: return false

        val targetPos = findSafeTeleportPos(level, respawnPos) ?: respawnPos.above().center
        teleportAndRevive(player, level, targetPos)
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
    }

    private fun findAnchorFromPlayerMemory(player: ServerPlayer): BlockPos? {
        val tag = player.persistentData
        if (!tag.contains(CLICKED_ANCHOR_TAG)) {
            return null
        }
        return BlockPos.of(tag.getLong(CLICKED_ANCHOR_TAG))
    }

    private fun findSafeTeleportPos(level: ServerLevel, anchorPos: BlockPos): Vec3? {
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
            val below = base
            val feetState = level.getBlockState(feet)
            val headState = level.getBlockState(head)
            val belowState = level.getBlockState(below)

            if (feetState.isAir && headState.isAir && belowState.isFaceSturdy(level, below, net.minecraft.core.Direction.UP)) {
                return Vec3(feet.x + 0.5, feet.y.toDouble(), feet.z + 0.5)
            }
        }

        return null
    }

    private fun hasNearbyEndPortal(level: Level, pos: BlockPos): Boolean {
        val max = MAX_PORTAL_DISTANCE.toInt()
        var nearest = Double.MAX_VALUE

        for (x in -max..max) {
            for (y in -max..max) {
                for (z in -max..max) {
                    val candidate = pos.offset(x, y, z)
                    val state = level.getBlockState(candidate)
                    if (!isEndPortalBlock(state)) {
                        continue
                    }

                    val distance = distance(pos, candidate)
                    if (distance < nearest) {
                        nearest = distance
                    }
                }
            }
        }

        return nearest <= MAX_PORTAL_DISTANCE
    }

    private fun isEndPortalBlock(state: BlockState): Boolean {
        return state.`is`(Blocks.END_PORTAL) || state.`is`(Blocks.END_PORTAL_FRAME)
    }

    private fun distance(a: BlockPos, b: BlockPos): Double {
        val dx = (a.x - b.x).toDouble()
        val dy = (a.y - b.y).toDouble()
        val dz = (a.z - b.z).toDouble()
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

