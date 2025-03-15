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

import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createdragonsplus.common.registry.CDPDataMaps;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class WrittenBookPrintingBehaviour implements PrintingBehaviour {
    private final WrittenBookContent content;
    private final boolean uncopiable;

    private WrittenBookPrintingBehaviour(WrittenBookContent content, boolean uncopiable) {
        this.content = content;
        this.uncopiable = uncopiable;
    }

    public static Optional<PrintingBehaviour> create(Level level, ItemStack stack) {
        if (!stack.is(Items.WRITTEN_BOOK))
            return Optional.empty();
        var content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            return Optional.of(new WrittenBookPrintingBehaviour(WrittenBookContent.EMPTY, false));
        }
        boolean uncopiable = content.generation() >= 2;
        content = new WrittenBookContent(
                content.title(),
                content.author(),
                0,
                content.pages(),
                content.resolved());
        return Optional.of(new WrittenBookPrintingBehaviour(content, uncopiable));
    }

    @Override
    public boolean isValid() {
        if (content == WrittenBookContent.EMPTY)
            return false;
        int capacity = CEIConfig.fluids().printerFluidCapacity.get();
        return content.pages().size() * 10 <= capacity;
    }

    @Override
    public boolean isSafeNBT() {
        return false; // Written books can contain contents unvailable for survival
    }

    @Override
    public int getRequiredItemCount(Level level, ItemStack stack) {
        if (stack.is(Items.BOOK))
            return 1;
        return 0;
    }

    @Override
    public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
        var color = fluidStack.getFluidHolder().getData(CDPDataMaps.FLUID_COLORING_CATALYST);
        if (color != DyeColor.BLACK)
            return 0;
        return content.pages().size() * 10;
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
        var result = stack.transmuteCopy(Items.WRITTEN_BOOK, 1);
        result.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return result;
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        if (uncopiable) {
            // TODO: Trigger advancement
        }
        // Plays SoundEvents.BOOK_PAGE_TURN
        level.levelEvent(1043, pos.below(), 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        var title = Component.literal(content.title().raw());
        var author = Component.translatable("book.byAuthor", content.author()).withStyle(ChatFormatting.GRAY);
        var cost = CEILang.number(content.pages().size() * 10)
                .add(CreateLang.translate("generic.unit.millibuckets"))
                .style(content.pages().size() * 10 > CEIConfig.fluids().printerFluidCapacity.get()
                        ? ChatFormatting.RED
                        : ChatFormatting.GREEN);
        CEILang.translate("gui.goggles.printing", title).forGoggles(tooltip);
        CEILang.builder().add(author).forGoggles(tooltip);
        CEILang.translate("gui.goggles.printing.written_book.cost", cost).forGoggles(tooltip);
        return true;
    }
}
