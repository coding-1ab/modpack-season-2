/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.registry;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static plus.dragons.createdragonsplus.data.recipe.VanillaRecipeBuilders.shaped;
import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindstoneBlock;
import plus.dragons.createenchantmentindustry.config.CEIStressConfig;

public class CEIBlocks {
    public static final BlockEntry<MechanicalGrindstoneBlock> MECHANICAL_GRINDSTONE = REGISTRATE
            .block("mechanical_grindstone", MechanicalGrindstoneBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(CEIStressConfig.setImpact(4.0))
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .item()
            .build()
            .recipe((ctx, prov) -> shaped()
                    .define('a', AllItems.ANDESITE_ALLOY)
                    .define('s', AllBlocks.SHAFT)
                    .pattern("aaa")
                    .pattern("asa")
                    .pattern("aaa")
                    .output(ctx.get())
                    .unlockedBy("has_andesite_alloy", DataIngredient.items(AllItems.ANDESITE_ALLOY.asItem()).getCriterion(prov))
                    .accept(prov))
            .register();
    public static final BlockEntry<GrindstoneDrainBlock> GRINDSTONE_DRAIN = REGISTRATE
            .block("grindstone_drain", GrindstoneDrainBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(CEIStressConfig.setImpact(4.0))
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .item()
            .transform(customItemModel())
            .recipe((ctx, prov) -> shaped()
                    .define('o', MECHANICAL_GRINDSTONE)
                    .define('=', AllBlocks.ITEM_DRAIN)
                    .pattern("o")
                    .pattern("=")
                    .output(ctx.get())
                    .unlockedBy("has_mechanical_grindstone", DataIngredient.items(MECHANICAL_GRINDSTONE.get()).getCriterion(prov))
                    .unlockedBy("has_item_drain", DataIngredient.items(AllBlocks.ITEM_DRAIN.get()).getCriterion(prov))
                    .accept(prov))
            .register();
    public static final BlockEntry<PrinterBlock> PRINTER = REGISTRATE
            .block("printer", PrinterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(pickaxeOnly())
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(AssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .recipe((ctx, prov) -> shaped()
                    .define('-', AllTags.commonItemTag("plates/brass"))
                    .define('o', AllBlocks.SPOUT)
                    .define('=', Tags.Items.STORAGE_BLOCKS_IRON)
                    .pattern("-")
                    .pattern("o")
                    .pattern("=")
                    .output(ctx.get())
                    .unlockedBy("has_brass_ingot", DataIngredient.tag(AllTags.commonItemTag("ingots/brass")).getCriterion(prov))
                    .unlockedBy("has_spout", DataIngredient.items(AllBlocks.SPOUT.get()).getCriterion(prov))
                    .accept(prov))
            .register();

    public static void register(IEventBus modBus) {}
}
