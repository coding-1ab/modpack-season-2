package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;

import net.createmod.catnip.utility.IntAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class RequestCraftableHelper {

	class CraftingDependency {

		private int amount;
		private List<ItemStack> options;

		CraftingDependency(int amount, List<ItemStack> options) {
			this.amount = amount;
			this.options = options;
		}

		boolean canReuseFor(Ingredient ingredient) {
			return options.stream()
				.allMatch(ingredient);
		}

	}

	record CraftableItem(ItemStack result, List<CraftingDependency> dependencies) {
	}

	private List<List<CraftableItem>> craftableByDepth;

	public void rebuild(InventorySummary baseItems) {
		List<BigItemStack> stacksByCount = baseItems.getStacksByCount();
		ClientLevel level = Minecraft.getInstance().level;
		RecipeManager recipeManager = level.getRecipeManager();
		RegistryAccess registryAccess = level.registryAccess();
		Set<Recipe<?>> checked = new HashSet<>();

		for (int depth = 0; depth < 3; depth++) {
			List<CraftableItem> itemsOnThisDepth = new ArrayList<>();

			recipeManager.getAllRecipesFor(RecipeType.CRAFTING)
				.parallelStream()
				.filter(r -> r.getSerializer() == RecipeSerializer.SHAPED_RECIPE
					|| r.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE)
				.filter(r -> r.getIngredients()
					.stream()
					.allMatch(i -> stacksByCount.stream()
						.anyMatch(b -> i.test(b.stack))
						|| craftableByDepth.stream()
							.anyMatch(l -> l.stream()
								.anyMatch(s -> i.test(s.result)))))

				.forEachOrdered(r -> {
					ItemStack resultItem = r.getResultItem(registryAccess);
					List<CraftingDependency> dependencies = new ArrayList<>();

					Ingredients: for (Ingredient ingredient : r.getIngredients()) {
						if (ingredient.isEmpty())
							continue;

						for (CraftingDependency existing : dependencies) {
							if (!existing.canReuseFor(ingredient))
								continue;
							existing.amount++;
							continue Ingredients;
						}

						List<ItemStack> list = stacksByCount.stream()
							.map(b -> b.stack)
							.filter(ingredient)
							.toList();

						for (List<CraftableItem> layer : craftableByDepth) {
							if (!list.isEmpty())
								break;
							list = layer.stream()
								.map(b -> b.result)
								.filter(ingredient)
								.toList();
						}
						
						dependencies.add(new CraftingDependency(1, list));
					}

					itemsOnThisDepth.add(new CraftableItem(resultItem, dependencies));
				});

			craftableByDepth.add(itemsOnThisDepth);
		}

	}

}
