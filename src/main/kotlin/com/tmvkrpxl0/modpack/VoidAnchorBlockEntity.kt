package com.tmvkrpxl0.modpack

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class VoidAnchorBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(BlockEntities.VOID_ANCHOR, pos, state) {

    var activeByPortal: Boolean = false

    override fun saveAdditional(tag: CompoundTag, registries: net.minecraft.core.HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.putBoolean("active_by_portal", activeByPortal)
    }

    override fun loadAdditional(tag: CompoundTag, registries: net.minecraft.core.HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        activeByPortal = tag.getBoolean("active_by_portal")
    }
}

