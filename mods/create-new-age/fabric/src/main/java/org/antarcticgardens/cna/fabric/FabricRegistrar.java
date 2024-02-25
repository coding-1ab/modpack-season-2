package org.antarcticgardens.cna.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

public class FabricRegistrar implements PlatformRegistrar {
    @Override
    public void registerConfig(ModConfig.Type type, IConfigSpec<?> spec) {
        ForgeConfigRegistry.INSTANCE.register(CreateNewAge.MOD_ID, type, spec);
    }

    @Override
    public RecipeType<?> registerRecipe(String name, RecipeSerializer<?> serializer) {
        RecipeType<?> type = RecipeType.register(name);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(CreateNewAge.MOD_ID, name), serializer);
        return type;
    }

    @Override
    public void beforeRegistration() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                CreateNewAge.CREATIVE_TAB_KEY.location(),
                FabricItemGroup.builder()
                        .icon(CNABlocks.GENERATOR_COIL::asStack)
                        .title(Component.translatable("tab." + CreateNewAge.MOD_ID + ".tab"))
                        .build());
    }

    @Override
    public void afterRegistration() {
        CreateNewAge.REGISTRATE.register();
    }
}
