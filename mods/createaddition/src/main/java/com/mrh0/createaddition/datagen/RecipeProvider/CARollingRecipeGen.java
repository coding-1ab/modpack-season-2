package com.mrh0.createaddition.datagen.RecipeProvider;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.datagen.RecipeGen.RollingRecipeGen;
import com.mrh0.createaddition.index.CAItems;
import com.simibubi.create.AllTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class CARollingRecipeGen extends RollingRecipeGen {
    public CARollingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateAddition.MODID);
    }
    private GeneratedRecipe wireRolling(Item wire, String metal) {
        return create(metal + "_plate", b -> b
                .require(AllTags.commonItemTag("plates/" + metal))
                .output(wire, 2));
    }
    private GeneratedRecipe rodRolling(Item rod, String metal) {
        return create(metal + "_ingot", b -> b
                .require(AllTags.commonItemTag("ingots/" + metal))
                .output(rod, 2));
    }

    GeneratedRecipe
            COPPER_ROD = rodRolling(CAItems.COPPER_ROD.get(), "copper"),
            COPPER_WIRE = wireRolling(CAItems.COPPER_WIRE.get(), "copper"),
            ELECTRUM_ROD = rodRolling(CAItems.ELECTRUM_ROD.get(), "electrum"),
            ELECTRUM_WIRE = wireRolling(CAItems.ELECTRUM_WIRE.get(), "electrum"),
            GOLD_ROD = rodRolling(CAItems.GOLD_ROD.get(), "gold"),
            GOLD_WIRE = wireRolling(CAItems.GOLD_WIRE.get(), "gold"),
            IRON_ROD = rodRolling(CAItems.IRON_ROD.get(), "iron"),
            IRON_WIRE = wireRolling( CAItems.IRON_WIRE.get(), "iron"),
            BRASS_ROD = create("brass_rod", b -> b
                    .require(AllTags.commonItemTag("ingots/brass"))
                    .output(CAItems.BRASS_ROD, 2)),
            STRAW = create(() -> Items.BAMBOO, b -> b.output(CAItems.STRAW)),
            STRAW2 = create(() -> Items.PAPER, b -> b.output(CAItems.STRAW));

}
