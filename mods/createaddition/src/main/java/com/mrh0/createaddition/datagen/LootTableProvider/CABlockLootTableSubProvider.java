//package com.mrh0.createaddition.datagen.LootTableProvider;
//
//import com.mrh0.createaddition.index.CABlocks;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.data.loot.BlockLootSubProvider;
//import net.minecraft.world.flag.FeatureFlags;
//
//import java.util.Set;
//
//public class CABlockLootTableSubProvider extends BlockLootSubProvider {
//
//
//    public CABlockLootTableSubProvider(HolderLookup.Provider provider) {
//        super(Set.of(), FeatureFlags.DEFAULT_FLAGS,provider);
//
//    }
//
//
//    @Override
//    protected void generate() {
//        dropSelf(CABlocks.ELECTRUM_BLOCK.get());
//    }
//}
