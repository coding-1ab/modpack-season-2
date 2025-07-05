//package com.mrh0.createaddition.datagen;
//
//import com.mrh0.createaddition.CreateAddition;
//import com.mrh0.createaddition.datagen.RecipeProvider.*;
//import com.mrh0.createaddition.datagen.TagProvider.CABlockTagProvider;
//import com.mrh0.createaddition.datagen.TagProvider.CAFluidTagProvider;
//import com.mrh0.createaddition.datagen.TagProvider.CAItemTagProvider;
//import com.simibubi.create.api.registry.CreateRegistries;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.core.RegistrySetBuilder;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.PackOutput;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.common.data.BlockTagsProvider;
//import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
//import net.neoforged.neoforge.common.data.ExistingFileHelper;
//import net.neoforged.neoforge.data.event.GatherDataEvent;
//
//import java.util.Set;
//import java.util.concurrent.CompletableFuture;
//
//@EventBusSubscriber(modid = CreateAddition.MODID, bus = EventBusSubscriber.Bus.MOD)
//public class CreateAdditionsDataGen {
//    @SubscribeEvent
//    public static void gatherData(GatherDataEvent event) {
//        System.out.println("gatherData HERE");
//        DataGenerator generator = event.getGenerator();
//        PackOutput output = generator.getPackOutput();
//        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
//        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
//
//        BlockTagsProvider blockTags = new CABlockTagProvider(output, lookupProvider, existingFileHelper);
//        generator.addProvider(event.includeServer(), blockTags);
//        generator.addProvider(event.includeServer(), new CAFluidTagProvider(output, lookupProvider, existingFileHelper));
//        generator.addProvider(event.includeServer(), new CAItemTagProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
//        generator.addProvider(event.includeServer(), new CACraftingRecipeProvider(output, lookupProvider));
//        generator.addProvider(event.includeServer(), new CACrushingRecipeGen(output, lookupProvider));
//        generator.addProvider(event.includeServer(), new CACompactingRecipeGen(output, lookupProvider));
//        generator.addProvider(event.includeServer(), new CAFillingRecipeGen(output, lookupProvider));
//        generator.addProvider(event.includeServer(), new CAMixingRecipeGen(output, lookupProvider));
//        generator.addProvider(event.includeServer(), new CAMechanicalCrafterRecipeGen(output, lookupProvider));
//        generator.addProvider(event.includeServer(), new CAPressingRecipeGen(output, lookupProvider));
//
//        generator.addProvider(event.includeServer(), new CAChargingRecipeProvider(output,lookupProvider));
//        generator.addProvider(event.includeServer(), new CARollingRecipeProvider(output,lookupProvider));
//        generator.addProvider(event.includeServer(), new CALiquidBurningRecipeProvider(output,lookupProvider));
//
//        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, lookupProvider, new RegistrySetBuilder()
//                .add(Registries.DAMAGE_TYPE, CADamageTypesDatagen::bootstrap)
//                .add(CreateRegistries.POTATO_PROJECTILE_TYPE, CAPotatoProjectileTypesDatagen::bootstrap),
//                Set.of(CreateAddition.MODID)
//        ));
//    }
//}
