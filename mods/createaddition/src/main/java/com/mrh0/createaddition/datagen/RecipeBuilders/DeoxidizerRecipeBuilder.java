package com.mrh0.createaddition.datagen.RecipeBuilders;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.recipe.charging.DeoxidizingRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class DeoxidizerRecipeBuilder implements RecipeBuilder {

    public static DeoxidizerRecipeBuilder special() {
        return new DeoxidizerRecipeBuilder();
    }

    @Override
    public RecipeBuilder unlockedBy(String s, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String s) {
        return this;
    }

    @Override
    public Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        DeoxidizingRecipe recipe = new DeoxidizingRecipe(
                "",
                4000,
                200
        );
        recipeOutput.accept(resourceLocation.withPrefix("charging/"), recipe, null);
    }

    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        save(recipeOutput, ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID,id));
    }
}
