package com.mrh0.createaddition.datagen.TagProvider;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.index.CABlocks;
import com.simibubi.create.AllTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CABlockTagProvider extends BlockTagsProvider {
    public CABlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CreateAddition.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING.tag).add(new Block[] {
                CABlocks.LIQUID_BLAZE_BURNER.get()
        });
        tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag).add(new Block[] {
                CABlocks.LIQUID_BLAZE_BURNER.get()
        });

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add( new Block[] {
                CABlocks.ALTERNATOR.get(),
                CABlocks.ELECTRIC_MOTOR.get(),
                CABlocks.ROLLING_MILL.get(),
                CABlocks.CREATIVE_ENERGY.get(),
                CABlocks.SMALL_CONNECTOR.get(),
                CABlocks.SMALL_LIGHT_CONNECTOR.get(),
                CABlocks.LARGE_CONNECTOR.get(),
                CABlocks.REDSTONE_RELAY.get(),
                CABlocks.TESLA_COIL.get(),
                CABlocks.LIQUID_BLAZE_BURNER.get(),
                CABlocks.BARBED_WIRE.get(),
                CABlocks.MODULAR_ACCUMULATOR.get(),
                CABlocks.PORTABLE_ENERGY_INTERFACE.get(),
                CABlocks.DIGITAL_ADAPTER.get()
        });

    }
}
