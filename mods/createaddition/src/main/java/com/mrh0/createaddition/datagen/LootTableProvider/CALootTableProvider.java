//package com.mrh0.createaddition.datagen.LootTableProvider;
//
//import net.minecraft.core.HolderLookup;
//import net.minecraft.data.PackOutput;
//import net.minecraft.data.loot.LootTableProvider;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
//
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.CompletableFuture;
//
//public class CALootTableProvider extends LootTableProvider {
//
//    public CALootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
//        super(output, Set.of(), List.of(new SubProviderEntry(
//                CABlockLootTableSubProvider::new,
//                LootContextParamSets.BLOCK
//        )),provider);
//    }
//
//}