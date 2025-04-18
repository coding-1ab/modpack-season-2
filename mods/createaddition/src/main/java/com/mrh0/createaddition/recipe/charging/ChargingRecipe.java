package com.mrh0.createaddition.recipe.charging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.index.CARecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public class ChargingRecipe implements Recipe<RecipeWrapper> {
	public Ingredient ingredient;
	public ItemStack output;
	public int energy;
	public int maxChargeRate;

	public ChargingRecipe(String group, Ingredient ingredient, ItemStack output, int energy, int maxChargeRate) {
		this.ingredient = ingredient;
		this.output = output;
		this.energy = energy;
		this.maxChargeRate = maxChargeRate;
	}

	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	@Override
	public boolean matches(@NotNull RecipeWrapper wrapper, @NotNull Level world) {
		if(ingredient == null) return false;
		return ingredient.test(wrapper.getItem(0));
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull RecipeWrapper recipeWrapper, HolderLookup.@NotNull Provider provider) {
		return output;
	}

	@Override
	public boolean canCraftInDimensions(int w, int h) {
		return true;
	}

	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
		return output;
	}

	public ItemStack getResultItem() {
		return output;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return CARecipes.CHARGING.get();
	}


	@Override
	public @NotNull RecipeType<?> getType() {
		return CARecipes.CHARGING_TYPE.get();
	}

	public int getEnergy() {
		return energy;
	}

	public int getMaxChargeRate() {
		return maxChargeRate;
	}

	public static class Serializer implements RecipeSerializer<ChargingRecipe> {
		private static final MapCodec<ChargingRecipe> CODEC = RecordCodecBuilder.mapCodec(
				builder -> builder.group(
								Codec.STRING.optionalFieldOf("group", "").forGetter(ChargingRecipe::getGroup),
								//CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ChargingRecipe::category),
								Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
								ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
								Codec.INT.optionalFieldOf("energy", 0).forGetter(r -> r.energy),
						Codec.INT.optionalFieldOf("max_charge_rate", 0).forGetter(r -> r.maxChargeRate)
						).apply(builder, ChargingRecipe::new)
		);

		public static final StreamCodec<RegistryFriendlyByteBuf, ChargingRecipe> STREAM_CODEC = StreamCodec.of(
				Serializer::toNetwork, Serializer::fromNetwork
		);

		@Override
		public MapCodec<ChargingRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ChargingRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static ChargingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			String group = buffer.readUtf();
			int maxChargeRate = buffer.readInt();
			int energy = buffer.readInt();
			ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
			Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			return new ChargingRecipe(group, input, output, energy, maxChargeRate);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, ChargingRecipe recipe) {
			buffer.writeUtf(recipe.getGroup());
			buffer.writeInt(recipe.maxChargeRate);
			buffer.writeInt(recipe.energy);
			ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
		}
	}
}
