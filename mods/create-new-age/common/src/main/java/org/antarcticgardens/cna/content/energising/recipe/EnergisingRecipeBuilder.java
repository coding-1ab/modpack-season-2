package org.antarcticgardens.cna.content.energising.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;

public class EnergisingRecipeBuilder extends ProcessingRecipeBuilder<EnergisingRecipe> {
    public EnergisingRecipeBuilder(ProcessingRecipeFactory<EnergisingRecipe> factory, ResourceLocation recipeId) {
        super(factory, recipeId);
        params = new EnergisingRecipeParams(recipeId);
    }
    
    public EnergisingRecipeBuilder energyNeeded(int energy) {
        ((EnergisingRecipeParams) params).energyNeeded = energy;
        return this;
    }
    
    public static class EnergisingRecipeParams extends ProcessingRecipeParams {
        protected int energyNeeded = 0;
        
        protected EnergisingRecipeParams(ResourceLocation id) {
            super(id);
        }
    }
}
