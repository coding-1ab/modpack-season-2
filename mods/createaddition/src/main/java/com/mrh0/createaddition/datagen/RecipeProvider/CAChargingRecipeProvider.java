package com.mrh0.createaddition.datagen.RecipeProvider;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.datagen.RecipeGen.ChargingRecipeGen;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.CopperBlockSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;

import java.util.concurrent.CompletableFuture;

public class CAChargingRecipeProvider extends ChargingRecipeGen {
    public CAChargingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
        super(output, registries, defaultNamespace);
    }

    public GeneratedRecipe buildCreateDeoxidizeVariants(CopperBlockSet.Variant<?> variant) {
        deoxidize(
                AllBlocks.COPPER_SHINGLES.get(variant, WeatheringCopper.WeatherState.OXIDIZED, false).get(),
                AllBlocks.COPPER_SHINGLES.get(variant, WeatheringCopper.WeatherState.WEATHERED, false).get()
        );
        deoxidize(
                AllBlocks.COPPER_SHINGLES.get(variant, WeatheringCopper.WeatherState.WEATHERED, false).get(),
                AllBlocks.COPPER_SHINGLES.get(variant, WeatheringCopper.WeatherState.EXPOSED, false).get()
        );
        deoxidize(
                AllBlocks.COPPER_SHINGLES.get(variant, WeatheringCopper.WeatherState.EXPOSED, false).get(),
                AllBlocks.COPPER_SHINGLES.get(variant, WeatheringCopper.WeatherState.UNAFFECTED, false).get()
        );

        deoxidize(
                AllBlocks.COPPER_TILES.get(variant, WeatheringCopper.WeatherState.OXIDIZED, false).get(),
                AllBlocks.COPPER_TILES.get(variant, WeatheringCopper.WeatherState.WEATHERED, false).get()
        );
        deoxidize(
                AllBlocks.COPPER_TILES.get(variant, WeatheringCopper.WeatherState.WEATHERED, false).get(),
                AllBlocks.COPPER_TILES.get(variant, WeatheringCopper.WeatherState.EXPOSED, false).get()
        );
        return deoxidize(
                AllBlocks.COPPER_TILES.get(variant, WeatheringCopper.WeatherState.EXPOSED, false).get(),
                AllBlocks.COPPER_TILES.get(variant, WeatheringCopper.WeatherState.UNAFFECTED, false).get()
        );
    }

    GeneratedRecipe
            CHANNELING = create(CreateAddition.asResource(Enchantments.CHANNELING.location().getPath()), (b) -> b.require(Items.BOOK)
            .enchantedOutput(new ItemStack(Items.ENCHANTED_BOOK), Enchantments.CHANNELING, provider)
            .energy(4000)
            .maxChargeRate(200)),

    OXIDIZED_COPPER = deoxidize(Blocks.OXIDIZED_COPPER),
            OXIDIZED_CHISELED_COPPER = deoxidize(Blocks.OXIDIZED_CHISELED_COPPER),
            OXIDIZED_COPPER_BULB = deoxidize(Blocks.OXIDIZED_COPPER_BULB),
            OXIDIZED_COPPER_DOOR = deoxidize(Blocks.OXIDIZED_COPPER_DOOR),
            OXIDIZED_COPPER_GRATE = deoxidize(Blocks.OXIDIZED_COPPER_GRATE),
            OXIDIZED_COPPER_TRAPDOOR = deoxidize(Blocks.OXIDIZED_COPPER_TRAPDOOR),
            OXIDIZED_CUT_COPPER = deoxidize(Blocks.OXIDIZED_CUT_COPPER),
            OXIDIZED_CUT_COPPER_SLAB = deoxidize(Blocks.OXIDIZED_CUT_COPPER_SLAB),
            OXIDIZED_CUT_COPPER_STAIRS = deoxidize(Blocks.OXIDIZED_CUT_COPPER_STAIRS),

    WEATHERED_COPPER = deoxidize(Blocks.WEATHERED_COPPER),
            WEATHERED_CHISELED_COPPER = deoxidize(Blocks.WEATHERED_CHISELED_COPPER),
            WEATHERED_COPPER_BULB = deoxidize(Blocks.WEATHERED_COPPER_BULB),
            WEATHERED_COPPER_DOOR = deoxidize(Blocks.WEATHERED_COPPER_DOOR),
            WEATHERED_COPPER_GRATE = deoxidize(Blocks.WEATHERED_COPPER_GRATE),
            WEATHERED_COPPER_TRAPDOOR = deoxidize(Blocks.WEATHERED_COPPER_TRAPDOOR),
            WEATHERED_CUT_COPPER = deoxidize(Blocks.WEATHERED_CUT_COPPER),
            WEATHERED_CUT_COPPER_SLAB = deoxidize(Blocks.WEATHERED_CUT_COPPER_SLAB),
            WEATHERED_CUT_COPPER_STAIRS = deoxidize(Blocks.WEATHERED_CUT_COPPER_STAIRS),

    EXPOSED_COPPER = deoxidize(Blocks.EXPOSED_COPPER),
            EXPOSED_CHISELED_COPPER = deoxidize(Blocks.EXPOSED_CHISELED_COPPER),
            EXPOSED_COPPER_BULB = deoxidize(Blocks.EXPOSED_COPPER_BULB),
            EXPOSED_COPPER_DOOR = deoxidize(Blocks.EXPOSED_COPPER_DOOR),
            EXPOSED_COPPER_GRATE = deoxidize(Blocks.EXPOSED_COPPER_GRATE),
            EXPOSED_COPPER_TRAPDOOR = deoxidize(Blocks.EXPOSED_COPPER_TRAPDOOR),
            EXPOSED_CUT_COPPER = deoxidize(Blocks.EXPOSED_CUT_COPPER),
            EXPOSED_CUT_COPPER_SLAB = deoxidize(Blocks.EXPOSED_CUT_COPPER_SLAB),
            EXPOSED_CUT_COPPER_STAIRS = deoxidize(Blocks.EXPOSED_CUT_COPPER_STAIRS),

    CREATE_BLOCK = buildCreateDeoxidizeVariants(CopperBlockSet.BlockVariant.INSTANCE),
            CREATE_STAIR = buildCreateDeoxidizeVariants(CopperBlockSet.StairVariant.INSTANCE),
            CREATE_SLAB = buildCreateDeoxidizeVariants(CopperBlockSet.SlabVariant.INSTANCE);
}
