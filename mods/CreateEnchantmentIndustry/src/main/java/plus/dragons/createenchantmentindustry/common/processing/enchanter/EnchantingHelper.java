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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;

public class EnchantingHelper {
    public static int getEnchantmentCost(Holder<Enchantment> holder, int level) {
        var enchantment = holder.value();
        int cost = enchantment.getMinCost(level);
        int anvilCost = enchantment.getAnvilCost();
        int experience = 0;
        for (int i = 0; i < anvilCost; i++) {
            experience += ExperienceHelper.getExperienceForLevel(cost++);
        }
        return experience;
    }

    public static int getEnchantmentCost(ItemEnchantments enchantments) {
        return enchantments.entrySet().stream()
                .mapToInt(entry -> getEnchantmentCost(entry.getKey(), entry.getIntValue()))
                .sum();
    }

    public static int getAdjustedEnchantLevel(RandomSource random, ItemStack stack, int level) {
        var enchantmentValue = stack.getEnchantmentValue();
        if (enchantmentValue > 0)
            level += 1 + random.nextInt(enchantmentValue / 4 + 1) + random.nextInt(enchantmentValue / 4 + 1);
        float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        level = Mth.clamp(Math.round(level + level * f), 1, Integer.MAX_VALUE);
        return level;
    }

    public static List<EnchantmentInstance> selectEnchantments(RandomSource random, int adjustedLevel, List<EnchantmentInstance> available) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        WeightedRandom.getRandomItem(random, available).ifPresent(list::add);
        while (random.nextInt(50) <= adjustedLevel) {
            if (!list.isEmpty())
                EnchantmentHelper.filterCompatibleEnchantments(available, Util.lastOf(list));

            if (available.isEmpty())
                break;

            WeightedRandom.getRandomItem(random, available).ifPresent(list::add);
            adjustedLevel /= 2;
        }
        return list;
    }
}
