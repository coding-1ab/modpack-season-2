package com.mrh0.createaddition.datagen;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.datagen.TagProvider.CABlockTagProvider;
import com.mrh0.createaddition.datagen.TagProvider.CAFluidTagProvider;
import com.mrh0.createaddition.datagen.TagProvider.CAItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CreateAddition.MODID,bus = EventBusSubscriber.Bus.MOD)
public class CreateAdditionsDatagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

//        BlockTagsProvider blockTags = new CABlockTagProvider(output,lookupProvider,existingFileHelper);
//        generator.addProvider(event.includeServer(),blockTags);
//        generator.addProvider(event.includeServer(),new CACraftingRecipeProvider(output,lookupProvider));
        generator.addProvider(event.includeServer(),new CAFluidTagProvider(output,lookupProvider,existingFileHelper));
//        generator.addProvider(event.includeServer(),new CAItemTagProvider(output,lookupProvider,blockTags.contentsGetter(),existingFileHelper));
    }
}
