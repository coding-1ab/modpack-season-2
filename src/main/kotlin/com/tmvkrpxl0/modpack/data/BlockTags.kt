package org.example.com.tmvkrpxl0.modpack.data

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import org.example.com.tmvkrpxl0.modpack.Blocks
import org.example.com.tmvkrpxl0.modpack.ModPackTweaks
import java.util.concurrent.CompletableFuture

class BlockTags(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
    helper: ExistingFileHelper
) :
    BlockTagsProvider(output, lookup, ModPackTweaks.ID, helper) {
    override fun addTags(provider: HolderLookup.Provider) {
        tag(BlockTags.FIRE).add(Blocks.ENDER_FIRE)
    }
}
