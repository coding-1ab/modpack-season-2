package com.mrh0.createaddition.datagen.RecipeBuilders;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

public class CALiquidBurningRecipeBuilder extends CARecipeBuilder {
    protected FluidIngredient ingredient;
    protected int burnTime;
    protected boolean superheated;

    public CALiquidBurningRecipeBuilder(int burnTime) {
        super(ItemStack.EMPTY);
        this.ingredient = FluidIngredient.EMPTY;
        this.burnTime = burnTime;
        this.superheated = false;
    }

    public static CALiquidBurningRecipeBuilder liquidBurning(int burnTime) {
        return new CALiquidBurningRecipeBuilder(burnTime);
    }

    public CALiquidBurningRecipeBuilder require(FluidIngredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public CALiquidBurningRecipeBuilder require(Fluid fluid) {
        this.ingredient = FluidIngredient.fromFluid(fluid, 1000);
        return this;
    }

    public CALiquidBurningRecipeBuilder require(TagKey<Fluid> fluidTag) {
        this.ingredient = FluidIngredient.fromTag(fluidTag, 1000);
        return this;
    }

    public CALiquidBurningRecipeBuilder superheated() {
        this.superheated = true;
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        LiquidBurningRecipe chargingRecipe = new LiquidBurningRecipe(
                Objects.requireNonNullElse(this.group, ""),
                this.ingredient,
                this.burnTime,
                this.superheated
        );
        recipeOutput.accept(resourceLocation.withPrefix("liquid_burning/"), chargingRecipe, null);
    }

    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        save(recipeOutput, ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, id));
    }
}
