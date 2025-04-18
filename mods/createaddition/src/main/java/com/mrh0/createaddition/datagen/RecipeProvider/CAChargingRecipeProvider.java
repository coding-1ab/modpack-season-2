package com.mrh0.createaddition.datagen.RecipeProvider;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.datagen.RecipeBuilders.ChargingRecipeBuilder;
import com.mrh0.createaddition.datagen.RecipeBuilders.DeoxidizerRecipeBuilder;
import com.mrh0.createaddition.index.CARecipes;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
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

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
//        CAChargingRecipeBuilder.charging(Items.COPPER_BLOCK)
//                .require(Items.EXPOSED_COPPER)
//                .energy(4000)
//                .maxChargeRate(200)
//                .save(output, ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, BuiltInRegistries.ITEM.getKey(Items.COPPER_BLOCK).getPath()));
        ChargingRecipeBuilder.charging(new ItemStack(Items.ENCHANTED_BOOK), Enchantments.CHANNELING, provider)
                .require(Items.BOOK)
                .maxChargeRate(1000)
                .energy(10000000)
                .save(output, ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID, Enchantments.CHANNELING.location().getPath()));
//        CAChargingRecipeBuilder.charging(AllBlocks.COPPER_SHINGLES.get( CopperBlockSet.SlabVariant.INSTANCE, WeatheringCopper.WeatherState.UNAFFECTED,false).asItem());
        DeoxidizerRecipeBuilder.special().save(output,"special_deoxidation");
        }


    @Override
    protected IRecipeTypeInfo getRecipeType() {
       return recipeType;
    }

}
