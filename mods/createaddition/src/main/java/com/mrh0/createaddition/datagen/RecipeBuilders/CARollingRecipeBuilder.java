package com.mrh0.createaddition.datagen.RecipeBuilders;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
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

public class CARollingRecipeBuilder extends CARecipeBuilder {
    protected Ingredient ingredient;

    public CARollingRecipeBuilder(ItemStack result) {
        super(result);
        this.ingredient = Ingredient.EMPTY;
    }

    public static CARollingRecipeBuilder rolling(ItemStack itemStack) {
        return new CARollingRecipeBuilder(itemStack);
    }

    public static CARollingRecipeBuilder rolling(ItemLike result, int count) {
        return rolling(new ItemStack(result,count));
    }

    public static CARollingRecipeBuilder rolling(ItemLike result) {
        return rolling(new ItemStack(result));
    }

    public static CARollingRecipeBuilder rolling(Item result, int count) {
        return rolling(new ItemStack(result,count));
    }


    public static CARollingRecipeBuilder rolling(Item result) {
        return rolling(new ItemStack(result));
    }

    public static CARollingRecipeBuilder rolling(ItemStack item, ResourceKey<Enchantment> enchantmentKey, HolderLookup.Provider provider) {
        item.enchant(provider.holderOrThrow(enchantmentKey),1);
        return rolling(item);
    }

    public CARollingRecipeBuilder require(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public CARollingRecipeBuilder require(ItemLike item) {
        this.require(Ingredient.of(item));
        return this;
    }

    public CARollingRecipeBuilder require(ItemStack item) {
        this.require(Ingredient.of(item));
        return this;
    }

    public CARollingRecipeBuilder require(ItemLike item, int count) {
        for(int i=0; i< count; ++i) {
            this.require(Ingredient.of(item));
        }
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        RollingRecipe rollingRecipe = new RollingRecipe(
                Objects.requireNonNullElse(this.group, ""),
                this.ingredient,
                this.result
        );
        recipeOutput.accept(resourceLocation.withPrefix("rolling/"), rollingRecipe, null);
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
