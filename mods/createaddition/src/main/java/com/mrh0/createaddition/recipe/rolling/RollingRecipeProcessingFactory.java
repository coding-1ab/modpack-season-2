package com.mrh0.createaddition.recipe.rolling;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class RollingRecipeProcessingFactory implements ProcessingRecipeBuilder.ProcessingRecipeFactory<RollingRecipe> {
    @Override
    public RollingRecipe create(ProcessingRecipeBuilder.ProcessingRecipeParams processingRecipeParams) {
        var params = (RollingMillRecipeParams) processingRecipeParams;
        Ingredient ingredient = Ingredient.EMPTY;
        ItemStack result = ItemStack.EMPTY;
        if (!params.getIngredients().isEmpty()) {
            ingredient = params.getIngredients().getFirst();
        }
        if (!params.getResults().isEmpty()) {
            result = params.getResults().getFirst().getStack();
        }
        return new RollingRecipe(params.getID().getPath(), ingredient, result);
    }
}
