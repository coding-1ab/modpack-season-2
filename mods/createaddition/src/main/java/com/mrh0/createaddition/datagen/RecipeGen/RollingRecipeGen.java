package com.mrh0.createaddition.datagen.RecipeGen;

import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.simibubi.create.api.data.recipe.StandardProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

import static com.mrh0.createaddition.recipe.rolling.RollingRecipe.TYPE_INFO;

public class RollingRecipeGen extends StandardProcessingRecipeGen<RollingRecipe> {
    public RollingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
        super(output, registries, defaultNamespace);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return TYPE_INFO;
    }
}
