package org.antarcticgardens.cna.forge.data;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.data.worldgen.CNAPlacedFeatures;

public class CNAForgeBiomeModifiers { 
    public static ResourceKey<BiomeModifier> THORIUM_ORE = key("thorium_ore");
    public static ResourceKey<BiomeModifier> MAGNETITE_BLOCK = key("magnetite_block");
    
    public static void bootstrap(BootstapContext<BiomeModifier> ctx) {
        HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);
        HolderSet<Biome> overworld = biomeLookup.getOrThrow(BiomeTags.IS_OVERWORLD);
        
        HolderGetter<PlacedFeature> featureLookup = ctx.lookup(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> thoriumOre = featureLookup.getOrThrow(CNAPlacedFeatures.THORIUM_ORE);
        Holder<PlacedFeature> magnetiteBlock = featureLookup.getOrThrow(CNAPlacedFeatures.MAGNETITE_BLOCK);
        
        ctx.register(THORIUM_ORE, addOre(overworld, thoriumOre));
        ctx.register(MAGNETITE_BLOCK, addOre(overworld, magnetiteBlock));
    }

    private static ResourceKey<BiomeModifier> key(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(CreateNewAge.MOD_ID, name));
    }

    private static ForgeBiomeModifiers.AddFeaturesBiomeModifier addOre(HolderSet<Biome> biomes, Holder<PlacedFeature> feature) {
        return new ForgeBiomeModifiers.AddFeaturesBiomeModifier(biomes, HolderSet.direct(feature), GenerationStep.Decoration.UNDERGROUND_ORES);
    }
}
