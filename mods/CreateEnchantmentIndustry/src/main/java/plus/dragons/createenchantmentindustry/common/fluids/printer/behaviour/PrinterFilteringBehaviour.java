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

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class PrinterFilteringBehaviour extends FilteringBehaviour {
    private PrintingBehaviour printing = new PrintingRecipeBehaviour(ItemStack.EMPTY);

    public PrinterFilteringBehaviour(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be, slot);
    }

    public PrintingBehaviour getPrinting() {
        return printing;
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        if (super.setFilter(stack)) {
            printing = PrintingBehaviour.create(getWorld(), stack);
            return true;
        }
        return false;
    }

    @Override
    public boolean isSafeNBT() {
        return printing.isSafeNBT();
    }

    @Override
    public void read(CompoundTag nbt, Provider registries, boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        printing = PrintingBehaviour.create(getWorld(), filter.item());
    }

    @Override
    public boolean writeToClipboard(Provider registries, CompoundTag tag, Direction side) {
        if (printing.isSafeNBT())
            return super.writeToClipboard(registries, tag, side);
        return false;
    }

    @Override
    public boolean readFromClipboard(Provider registries, CompoundTag tag, Player player, Direction side, boolean simulate) {
        var originalFilter = filter.item();
        if (!super.readFromClipboard(registries, tag, player, side, simulate))
            return false;
        if (!printing.isSafeNBT() && !simulate) {
            var newFilter = filter.item();
            player.displayClientMessage(CEILang
                    .translate("logistics.filter.requires_actual_item", newFilter.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                    .style(ChatFormatting.RED)
                    .component(), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            setFilter(originalFilter);
            return false;
        }
        return true;
    }
}
