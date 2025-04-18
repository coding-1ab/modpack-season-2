package com.mrh0.createaddition.datagen.RecipeProvider;

import com.mrh0.createaddition.datagen.RecipeBuilders.CALiquidBurningRecipeBuilder;
import com.mrh0.createaddition.datagen.TagProvider.CATagRegister;
import com.mrh0.createaddition.index.CARecipes;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CALiquidBurningRecipeProvider extends ProcessingRecipeGen {
    public static final IRecipeTypeInfo recipeType = new IRecipeTypeInfo() {
        @Override
        public ResourceLocation getId() {
            return CARecipes.LIQUID_BURNING.getId();
        }

        @Override
        public <T extends RecipeSerializer<?>> T getSerializer() {
            return (T) CARecipes.LIQUID_BURNING.get();
        }

        @Override
        public <V extends RecipeInput, R extends Recipe<V>> RecipeType<R> getType() {
            return (RecipeType<R>) CARecipes.LIQUID_BURNING_TYPE.get();
        }
    };

    private final HolderLookup.Provider provider;

    public CALiquidBurningRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        try {
            this.provider = registries.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        CALiquidBurningRecipeBuilder.liquidBurning(24000)
                .require(CATagRegister.Fluids.BIOFUEL)
                .superheated()
                .save(output, "biofuel");
        CALiquidBurningRecipeBuilder.liquidBurning(4800)
                .require(CATagRegister.Fluids.PLANTOIL)
                .save(output, "plantoil");
        CALiquidBurningRecipeBuilder.liquidBurning(20000)
                .require(FluidTags.LAVA)
                .save(output, "lava");
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
       return recipeType;
    }
}
