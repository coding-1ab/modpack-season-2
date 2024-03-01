package org.antarcticgardens.cna.platform;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public interface PlatformRegistrar {
    void registerConfig(ModConfig.Type type, IConfigSpec<?> spec);
    RecipeType<?> registerRecipe(String name, RecipeSerializer<?> serializer);
    void registerCustomItemRenderer(Item item, CustomRenderedItemModelRenderer renderer);
    
    default void beforeRegistration() {  }
    default void afterRegistration() {  }
}
