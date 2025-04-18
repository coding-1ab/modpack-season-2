package com.mrh0.createaddition.datagen.RecipeBuilders;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;

public class CAChargingRecipeBuilder extends CARecipeBuilder {
    protected Ingredient ingredient;
    protected int energy , maxChargeRate;

    public CAChargingRecipeBuilder(ItemStack result) {
        super(result);
        this.ingredient = Ingredient.EMPTY;
        this.energy = 0;
        this.maxChargeRate = 0;
    }

    public static CAChargingRecipeBuilder charging(ItemStack itemStack) {
        return new CAChargingRecipeBuilder(itemStack);
    }

    public static CAChargingRecipeBuilder charging(ItemLike result, int count) {
        return charging(new ItemStack(result,count));
    }

    public static CAChargingRecipeBuilder charging(ItemLike result) {
        return charging(new ItemStack(result));
    }

    public static CAChargingRecipeBuilder charging(Item result, int count) {
        return charging(new ItemStack(result,count));
    }


    public static CAChargingRecipeBuilder charging(Item result) {
        return charging(new ItemStack(result));
    }

    public static CAChargingRecipeBuilder charging(ItemStack item, ResourceKey<Enchantment> enchantmentKey, HolderLookup.Provider provider) {
        item.enchant(provider.holderOrThrow(enchantmentKey),1);
        return charging(item);
    }

    public CAChargingRecipeBuilder require(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public CAChargingRecipeBuilder require(ItemLike item) {
        this.require(Ingredient.of(item));
        return this;
    }

    public CAChargingRecipeBuilder require(ItemStack item) {
        this.require(Ingredient.of(item));
        return this;
    }

    public CAChargingRecipeBuilder require(ItemLike item, int count) {
        for(int i=0; i< count; ++i) {
            this.require(Ingredient.of(item));
        }
        return this;
    }

    public CAChargingRecipeBuilder energy(int energy) {
        this.energy = energy;
        return this;
    }

    public CAChargingRecipeBuilder maxChargeRate(int maxChargeRate) {
        this.maxChargeRate = maxChargeRate;
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        ChargingRecipe chargingRecipe = new ChargingRecipe(
                Objects.requireNonNullElse(this.group, ""),
                this.ingredient,
                this.result,
                this.energy,
                this.maxChargeRate
        );
        recipeOutput.accept(resourceLocation.withPrefix("charging/"), chargingRecipe, null);
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        save(recipeOutput, BuiltInRegistries.ITEM.getKey(this.result.getItem()));
    }

    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        save(recipeOutput, ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, id));
    }
}
