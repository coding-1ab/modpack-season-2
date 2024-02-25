package org.antarcticgardens.cna.util;

import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.antarcticgardens.cna.CreateNewAge;

public class RecipeUtil {
    public static IRecipeTypeInfo createIRecipeTypeInfo(String name, RecipeSerializer<?> serializer) {
        RecipeType<?> type = CreateNewAge.getInstance().getPlatform().getRegistrar().registerRecipe(name, serializer);

        return new IRecipeTypeInfo() {
            @Override
            public ResourceLocation getId() {
                return new ResourceLocation(CreateNewAge.MOD_ID, name);
            }

            @Override
            public <T extends RecipeSerializer<?>> T getSerializer() {
                return (T) serializer;
            }

            @Override
            public <T extends RecipeType<?>> T getType() {
                return (T) type;
            }
        };
    }
}
