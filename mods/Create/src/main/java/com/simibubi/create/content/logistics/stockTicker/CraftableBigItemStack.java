package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class CraftableBigItemStack extends BigItemStack {

	public Recipe<?> recipe;

	public CraftableBigItemStack(ItemStack stack, Recipe<?> recipe) {
		super(stack);
		this.recipe = recipe;
	}
	
	public List<Ingredient> getIngredients() {
		return recipe.getIngredients();
	}

}
