package org.antarcticgardens.cna.platform;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public interface PlatformRegistrar {
    void registerConfig(ModConfig.Type type, IConfigSpec<?> spec);
    RecipeType<?> registerRecipe(String name, RecipeSerializer<?> serializer);
    
    default void beforeRegistration() {  }
    default void afterRegistration() {  }
}
