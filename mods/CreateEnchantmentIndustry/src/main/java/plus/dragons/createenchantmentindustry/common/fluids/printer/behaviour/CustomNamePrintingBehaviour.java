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

package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class CustomNamePrintingBehaviour implements PrintingBehaviour {
    private static final Component EMPTY = Component.empty();
    private final Component name;

    private CustomNamePrintingBehaviour(Component name) {
        this.name = name;
    }

    public static Optional<PrintingBehaviour> create(Level level, ItemStack stack) {
        if (stack.is(Items.NAME_TAG)) {
            var name = stack.get(DataComponents.CUSTOM_NAME);
            return Optional.of(new CustomNamePrintingBehaviour(name == null ? EMPTY : name.copy()));
        }
        return Optional.empty();
    }

    @Override
    public boolean isValid() {
        return name != EMPTY;
    }

    @Override
    public int getRequiredItemCount(Level level, ItemStack stack) {
        return stack.getCount();
    }

    @Override
    public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
        int xp = ExperienceHelper.getExperienceFromFluid(fluidStack);
        if (xp > 0)
            return ExperienceHelper.getFluidFromExperience(fluidStack, 25);
        if (fluidStack.getFluidHolder().getData(CDPDataMaps.FLUID_COLORING_CATALYST) != null)
            return 250;
        return 0;
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
        var result = stack.copy();
        var name = this.name.copy();
        var color = fluidStack.getFluidHolder().getData(CDPDataMaps.FLUID_COLORING_CATALYST);
        if (color != null)
            name.setStyle(name.getStyle().withColor(color.getTextColor()));
        if (CEIConfig.fluids().printingCustomNameRemovesItalic.get())
            name.setStyle(name.getStyle().withItalic(false));
        result.set(DataComponents.CUSTOM_NAME, name);
        return result;
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        // TODO: Trigger advancement
        // Plays SoundEvents.ANVIL_USE
        level.levelEvent(1030, pos.below(), 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEILang.translate("gui.goggles.printing.custom_name", name).forGoggles(tooltip);
        return PrintingBehaviour.super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}
