package com.mrh0.createaddition.datagen.RecipeProvider;

import com.mrh0.createaddition.index.CARecipes;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CARollingRecipeProvider extends ProcessingRecipeGen {
    public static final IRecipeTypeInfo recipeType = new IRecipeTypeInfo() {
        @Override
        public ResourceLocation getId() {
            return CARecipes.ROLLING.getId();
        }

        @Override
        public <T extends RecipeSerializer<?>> T getSerializer() {
            return (T) CARecipes.ROLLING.get();
        }

        @Override
        public <V extends RecipeInput, R extends Recipe<V>> RecipeType<R> getType() {
            return (RecipeType<R>) CARecipes.ROLLING_TYPE.get();
        }
    };

    private final HolderLookup.Provider provider;

    public CARollingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        try {
            this.provider = registries.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        /*
        CARollingRecipeBuilder.rolling(CAItems.STRAW)
                .require(Items.BAMBOO)
                .save(output);

        CARollingRecipeBuilder.rolling(CAItems.BRASS_ROD)
                .require(AllItems.BRASS_INGOT)
                .save(output);
        CARollingRecipeBuilder.rolling(CAItems.COPPER_ROD)
                .require(Items.COPPER_INGOT)
                .save(output);
        CARollingRecipeBuilder.rolling(CAItems.ELECTRUM_ROD)
                .require(CAItems.ELECTRUM_INGOT)
                .save(output);
        CARollingRecipeBuilder.rolling(CAItems.IRON_ROD)
                .require(Items.IRON_INGOT)
                .save(output);
        CARollingRecipeBuilder.rolling(CAItems.GOLD_ROD)
                .require(Items.GOLD_INGOT)
                .save(output);
        */
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
       return recipeType;
    }
}
