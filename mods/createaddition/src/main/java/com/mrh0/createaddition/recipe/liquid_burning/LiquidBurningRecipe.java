package com.mrh0.createaddition.recipe.liquid_burning;

import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.FluidRecipeWrapper;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class LiquidBurningRecipe extends StandardProcessingRecipe<FluidRecipeWrapper> {
    public static final IRecipeTypeInfo TYPE_INFO = new IRecipeTypeInfo() {
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

    protected int burnTime;
    protected boolean superheated;

    public LiquidBurningRecipe(ProcessingRecipeParams params) {
        super(TYPE_INFO, params);
    }

    @Override
    public boolean matches(@NotNull FluidRecipeWrapper wrapper, @NotNull Level world) {
        if (fluidIngredients == null) return false;
        if (wrapper.fluid == null) return false;
        return getRequiredFluid().test(wrapper.fluid);
    }

    public FluidIngredient getRequiredFluid() {
        if (fluidIngredients.isEmpty())
            throw new IllegalStateException("Filling Recipe has no fluid ingredient!");
        return fluidIngredients.get(0);
    }

    public int getBurnTime() {
        return processingDuration;
    }

    public boolean isSuperheated() {
        return requiredHeat == HeatCondition.SUPERHEATED;
    }

    @Override
    protected int getMaxInputCount() {
        return 0;
    }

    @Override
    protected int getMaxOutputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }
}