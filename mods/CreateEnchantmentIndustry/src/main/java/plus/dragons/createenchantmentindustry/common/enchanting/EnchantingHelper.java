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

package plus.dragons.createenchantmentindustry.common.enchanting;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
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
}
