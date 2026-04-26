package com.tmvkrpxl0.modpack.data

import com.mrh0.createaddition.datagen.TagProvider.CATagRegister
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.FluidTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import com.tmvkrpxl0.modpack.Fluids
import com.tmvkrpxl0.modpack.ModPackTweaks
import com.tmvkrpxl0.modpack.toResource
import net.minecraft.tags.FluidTags
import java.util.concurrent.CompletableFuture

class FluidTags(
    output: PackOutput,
    provider: CompletableFuture<HolderLookup.Provider>,
    existingFileHelper: ExistingFileHelper
) : FluidTagsProvider(output, provider, ModPackTweaks.ID, existingFileHelper) {
    companion object {
        val ENDER_FUEL = FluidTags.create("ender_fuel".toResource())
    }

    override fun addTags(provider: HolderLookup.Provider) {
        tag(CATagRegister.Fluids.IGNITES).add(Fluids.ENDER_FUEL, Fluids.ENDER_FUEL_FLOWING)
        tag(ENDER_FUEL).add(Fluids.ENDER_FUEL, Fluids.ENDER_FUEL_FLOWING)
    }
}
