package dev.codinglabs.modpack

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class VoidAnchorBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(BlockEntities.VOID_ANCHOR, pos, state)
