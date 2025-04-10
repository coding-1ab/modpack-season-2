package com.mrh0.createaddition.recipe.rolling;

import com.mrh0.createaddition.CreateAddition;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class RollingRecipeInfo implements IRecipeTypeInfo {
    private SequencedAssemblyRollingRecipeSerializer serializer;
    private RecipeType<RollingRecipe> type;

    public RollingRecipeInfo(SequencedAssemblyRollingRecipeSerializer serializer, RecipeType<RollingRecipe> type) {
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public ResourceLocation getId() {
        return CreateAddition.asResource("rolling");
    }

    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializer;
    }

    @Override
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type;
    }
}
