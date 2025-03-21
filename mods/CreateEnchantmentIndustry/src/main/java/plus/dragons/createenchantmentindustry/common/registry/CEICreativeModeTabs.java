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

import static plus.dragons.createenchantmentindustry.common.registry.CEIBlocks.*;
import static plus.dragons.createenchantmentindustry.common.registry.CEIItems.*;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeColors;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class CEICreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, CEICommon.ID);
    public static final Holder<CreativeModeTab> BASE = TABS.register("base", CEICreativeModeTabs::base);

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private static CreativeModeTab base(ResourceLocation id) {
        return CreativeModeTab.builder()
                .title(CEILang.description("itemGroup", id).component())
                .withTabsBefore(AllCreativeModeTabs.BASE_CREATIVE_TAB.getId())
                .icon(CEIBlocks.PRINTER::asStack)
                .displayItems(CEICreativeModeTabs::buildBaseContents)
                .build();
    }

    private static void buildBaseContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(MECHANICAL_GRINDSTONE);
        output.accept(PRINTER);
        output.accept(AllBlocks.EXPERIENCE_BLOCK);
        output.accept(AllItems.EXP_NUGGET);
        output.accept(EXPERIENCE_CAKE_BASE, TabVisibility.SEARCH_TAB_ONLY);
        output.accept(EXPERIENCE_CAKE);
        output.accept(EXPERIENCE_BUCKET);
        for (var color : DyeColors.CREATIVE_MODE_TAB) {
            CDPFluids.DYES_BY_COLOR.get(color).getBucket().ifPresent(output::accept);
        }
    }
}
