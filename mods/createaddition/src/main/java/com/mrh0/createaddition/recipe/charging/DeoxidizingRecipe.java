package com.mrh0.createaddition.recipe.charging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrh0.createaddition.index.CARecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class DeoxidizingRecipe extends ChargingRecipe implements Recipe<RecipeWrapper> {
    public String group;
    public int energy;
    public int maxChargeRate;
    private Item result;

    public DeoxidizingRecipe(@Nullable String group, int energy, int maxChargeRate) {
        super(group, Ingredient.EMPTY, ItemStack.EMPTY, energy, maxChargeRate);
        this.group = group;
        this.energy = energy;
        this.maxChargeRate = maxChargeRate;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean matches(RecipeWrapper recipeWrapper, Level level) {
        Item item = recipeWrapper.getItem(0).getItem();
        if (item instanceof BlockItem blockItem) {
            Block currentBlock = blockItem.getBlock();
            Optional<Block> deoxidizedBlock = Optional.ofNullable(DataMapHooks.getPreviousOxidizedStage(currentBlock));
            if (deoxidizedBlock.isEmpty()) return false;
            result = deoxidizedBlock.get().asItem();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper recipeWrapper, HolderLookup.Provider provider) {
        return getResultItem(provider);
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result.getDefaultInstance();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CARecipes.DEOXIDIZING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return CARecipes.CHARGING_TYPE.get();
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxChargeRate() {
        return maxChargeRate;
    }

    public static class Serializer implements RecipeSerializer<DeoxidizingRecipe> {
        private final MapCodec<DeoxidizingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.optionalFieldOf("group","").forGetter(DeoxidizingRecipe::getGroup),
                    Codec.INT.optionalFieldOf("energy", 0).forGetter(DeoxidizingRecipe::getEnergy),
                    Codec.INT.optionalFieldOf("max_charge_rate", 0).forGetter(DeoxidizingRecipe::getMaxChargeRate)
            ).apply(builder,DeoxidizingRecipe::new));

        private final StreamCodec<RegistryFriendlyByteBuf, DeoxidizingRecipe> STREAMCODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static DeoxidizingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int maxChargeRate = buffer.readInt();
            int energy = buffer.readInt();
            return new DeoxidizingRecipe(group,energy,maxChargeRate);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, DeoxidizingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeInt(recipe.maxChargeRate);
            buffer.writeInt(recipe.energy);
        }

        @Override
        public MapCodec<DeoxidizingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DeoxidizingRecipe> streamCodec() {
            return STREAMCODEC;
        }

    }

}
