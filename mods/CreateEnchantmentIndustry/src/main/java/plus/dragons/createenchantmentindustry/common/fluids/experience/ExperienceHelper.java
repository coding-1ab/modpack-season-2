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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

public class ExperienceHelper {
    public static int getExperienceForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    public static int getExperienceFromFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return 0;
        int amount = fluid.getAmount();
        ExperienceRatio ratio = fluid.getFluidHolder().getData(CEIDataMaps.FLUID_EXPERIENCE_RATIO);
        if (ratio == null)
            return 0;
        return ratio.fromExperience() ? amount / ratio.ratio() : amount * ratio.ratio();
    }

    public static int getFluidFromExperience(FluidStack fluid, int amount) {
        return getFluidFromExperience(fluid.getFluidHolder(), amount);
    }

    public static int getFluidFromExperience(Holder<Fluid> fluid, int amount) {
        ExperienceRatio ratio = fluid.getData(CEIDataMaps.FLUID_EXPERIENCE_RATIO);
        if (ratio == null)
            return 0;
        return ratio.fromExperience()
               ? amount * ratio.ratio()
               : amount / ratio.ratio();
    }

    public static FluidStack getExperienceFluid(int amount) {
        return amount == 0 ? FluidStack.EMPTY : new FluidStack(CEIFluids.EXPERIENCE.get(), amount);
    }

    public static void award(int amount, ServerPlayer player) {
        amount = repairPlayerItems(player, amount);
        player.giveExperiencePoints(amount);
    }

    public static boolean canRepairItem(ItemStack stack) {
        if (!stack.isDamaged())
            return false;
        var lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        if (lookup == null)
            return false;
        ItemEnchantments enchantments = stack.getAllEnchantments(lookup);
        for (var enchantment : enchantments.keySet()) {
            if (enchantment.value().effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP))
                return true;
        }
        return false;
    }

    public static int repairItem(int amount, ServerLevel level, ItemStack stack, boolean simulate) {
        int repairing = EnchantmentHelper.modifyDurabilityToRepairFromXp(level, stack, (int) (amount * stack.getXpRepairRatio()));
        int repaired = Math.min(repairing, stack.getDamageValue());
        if (repaired == 0)
            return 0;
        if (!simulate) {
            stack.setDamageValue(stack.getDamageValue() - repaired);
        }
        return Math.max(1, repaired * amount / repairing);
    }

    public static int repairPlayerItems(ServerPlayer player, int amount) {
        Optional<EnchantedItemInUse> optional = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (optional.isPresent()) {
            ItemStack stack = optional.get().itemStack();
            int consumed = repairItem(amount, player.serverLevel(), stack, false);
            return amount > consumed
                   ? repairPlayerItems(player, amount - consumed)
                   : 0;
        }
        return amount;
    }
}
