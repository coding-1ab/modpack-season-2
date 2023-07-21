package org.antarcticgardens.newage.tools;

import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.antarcticgardens.newage.CreateNewAge;

public class RecipeTool {
    public static  <S extends RecipeSerializer<?>> IRecipeTypeInfo createIRecipeInfo(String name, S serializer) {
        RecipeType<?> type = RegistryTool.registerRecipe(name);
        RegistryTool.registerSerializer(name, serializer);
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
