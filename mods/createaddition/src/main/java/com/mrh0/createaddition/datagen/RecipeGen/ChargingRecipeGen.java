package com.mrh0.createaddition.datagen.RecipeGen;

import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.recipe.charging.ChargingRecipeParams;
import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

import static com.mrh0.createaddition.recipe.charging.ChargingRecipe.TYPE_INFO;


public class ChargingRecipeGen extends ProcessingRecipeGen<ChargingRecipeParams, ChargingRecipe, ChargingRecipe.Builder<ChargingRecipe>> {
    public ChargingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
        super(output, registries, defaultNamespace);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return TYPE_INFO;
    }

    @Override
    protected ChargingRecipe.Builder<ChargingRecipe> getBuilder(ResourceLocation id) {
        return null;
    }
}
