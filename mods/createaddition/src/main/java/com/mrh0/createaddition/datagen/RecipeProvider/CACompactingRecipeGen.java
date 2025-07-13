package com.mrh0.createaddition.datagen.RecipeProvider;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.index.CAFluids;
import com.mrh0.createaddition.index.CAItems;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.CompactingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class CACompactingRecipeGen extends CompactingRecipeGen {

    @SuppressWarnings("unused")
    GeneratedRecipe

    BIOMASS_PELLET = create(CAItems.BIOMASS_PELLET.getId(), b -> b.require(CAItems.BIOMASS)
            .output(Fluids.WATER,50)
            .output(CAItems.BIOMASS_PELLET)
    ),
    CAKE_BASE = create(CAItems.CAKE_BASE.getId(), b -> b.require(Tags.Items.EGGS)
            .require(Items.SUGAR)
            .require(Items.SUGAR)
            .require(AllItems.DOUGH)
            .output(CAItems.CAKE_BASE)
    ),
    SEED_OIL = create(ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID,"seed_oil"), b -> b.require(Tags.Items.SEEDS)
            .output( CAFluids.SEED_OIL.getSource().getSource(), 100)
    )
    ;

    public CACompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateAddition.MODID);
    }
}
