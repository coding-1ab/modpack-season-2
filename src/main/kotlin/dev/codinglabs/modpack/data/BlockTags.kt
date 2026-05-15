package dev.codinglabs.modpack.data

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import dev.codinglabs.modpack.Blocks
import plus.dragons.createdragonsplus.common.registry.CDPBlocks
import java.util.concurrent.CompletableFuture
import dev.codinglabs.modpack.ID

class BlockTags(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
    helper: ExistingFileHelper
) :
    BlockTagsProvider(output, lookup, ID, helper) {
    override fun addTags(provider: HolderLookup.Provider) {
        tag(BlockTags.FIRE).add(Blocks.ENDER_FIRE)
        tag(CDPBlocks.MOD_TAGS.fanEndingCatalysts).add(Blocks.ENDER_FIRE)
    }
}
