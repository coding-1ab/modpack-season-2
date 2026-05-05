package dev.codinglabs.modpack

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.UUID

class VoidAnchorBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BlockEntities.VOID_ANCHOR, pos, state) {

    var owner: UUID? = null
        private set

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        owner?.let { tag.putUUID("Owner", it) }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        owner = if (tag.hasUUID("Owner")) tag.getUUID("Owner") else null
    }
}
