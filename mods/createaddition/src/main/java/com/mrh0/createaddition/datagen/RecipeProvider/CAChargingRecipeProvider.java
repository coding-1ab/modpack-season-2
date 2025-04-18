package com.mrh0.createaddition.datagen.RecipeProvider;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.datagen.RecipeBuilders.CAChargingRecipeBuilder;
import com.mrh0.createaddition.index.CARecipes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.CopperBlockSet;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.WeatheringCopper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CAChargingRecipeProvider extends ProcessingRecipeGen {
    public static final IRecipeTypeInfo recipeType = new IRecipeTypeInfo() {
        @Override
        public ResourceLocation getId() {
            return CARecipes.CHARGING.getId();
        }

        @Override
        public <T extends RecipeSerializer<?>> T getSerializer() {
            return (T) CARecipes.CHARGING.get();
        }

        @Override
        public <V extends RecipeInput, R extends Recipe<V>> RecipeType<R> getType() {
            return (RecipeType<R>) CARecipes.CHARGING_TYPE.get();
        }
    };
    private final HolderLookup.Provider provider;

    public CAChargingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        try {
            this.provider = registries.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void unoxidizing(RecipeOutput output, Item oxidized, Item unoxidized) {
        CAChargingRecipeBuilder.charging(unoxidized)
                .require(oxidized)
                .energy(4000)
                .maxChargeRate(200)
                .save(output);
    }

    private void unoxidizeChain(RecipeOutput output, Item normal, Item exposed, Item weathered, Item oxidized) {
        unoxidizing(output, exposed, normal);
        unoxidizing(output, weathered, exposed);
        unoxidizing(output, oxidized, weathered);
    }

    private void unoxidizeSet(RecipeOutput output, CopperBlockSet set) {
        for (CopperBlockSet.Variant<?> variant : set.getVariants()) {
            for(WeatheringCopper.WeatherState state : WeatheringCopper.WeatherState.values()) {
                if(state != WeatheringCopper.WeatherState.UNAFFECTED) {
                    WeatheringCopper.WeatherState previous = WeatheringCopper.WeatherState.values()[state.ordinal() - 1];
                    unoxidizing(output, set.get(variant, state, false).asItem(), set.get(variant, previous, false).asItem());
                }
            }
        }
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        unoxidizeChain(output, Items.COPPER_BLOCK, Items.EXPOSED_COPPER, Items.WEATHERED_COPPER, Items.OXIDIZED_COPPER);
        
        unoxidizeChain(output, Items.CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.WEATHERED_CUT_COPPER, Items.OXIDIZED_CUT_COPPER);
        unoxidizeChain(output, Items.CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER_SLAB, Items.WEATHERED_CUT_COPPER_SLAB, Items.OXIDIZED_CUT_COPPER_SLAB);
        unoxidizeChain(output, Items.CUT_COPPER_STAIRS, Items.EXPOSED_CUT_COPPER_STAIRS, Items.WEATHERED_CUT_COPPER_STAIRS, Items.OXIDIZED_CUT_COPPER_STAIRS);

        unoxidizeSet(output, AllBlocks.COPPER_SHINGLES);

        CAChargingRecipeBuilder.charging(new ItemStack(Items.ENCHANTED_BOOK), Enchantments.CHANNELING, provider)
                .require(Items.BOOK)
                .maxChargeRate(1000)
                .energy(10000000)
                .save(output, ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, Enchantments.CHANNELING.location().getPath()));
//        CAChargingRecipeBuilder.charging(AllBlocks.COPPER_SHINGLES.get( CopperBlockSet.SlabVariant.INSTANCE, WeatheringCopper.WeatherState.UNAFFECTED,false).asItem());

    }


    @Override
    protected IRecipeTypeInfo getRecipeType() {
       return recipeType;
    }

}
