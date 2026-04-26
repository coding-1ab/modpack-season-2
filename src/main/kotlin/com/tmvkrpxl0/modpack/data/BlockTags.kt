package com.tmvkrpxl0.modpack.data

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import com.tmvkrpxl0.modpack.Blocks
import com.tmvkrpxl0.modpack.ModPackTweaks
import plus.dragons.createdragonsplus.common.registry.CDPBlocks
import java.util.concurrent.CompletableFuture

class BlockTags(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
    helper: ExistingFileHelper
) :
    BlockTagsProvider(output, lookup, ModPackTweaks.ID, helper) {
    override fun addTags(provider: HolderLookup.Provider) {
        tag(BlockTags.FIRE).add(Blocks.ENDER_FIRE)
        tag(CDPBlocks.MOD_TAGS.fanEndingCatalysts).add(Blocks.ENDER_FIRE)
    }
}
