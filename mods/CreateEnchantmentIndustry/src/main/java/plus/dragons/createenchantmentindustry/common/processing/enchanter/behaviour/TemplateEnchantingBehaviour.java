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

package plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingHelper;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataComponents;

public class TemplateEnchantingBehaviour extends EnchantingBehaviour {
    private final ItemStack target;

    public TemplateEnchantingBehaviour(ItemStack target) {
        this.target = target;
    }

    @Override
    protected List<EnchantmentInstance> getAvailableEnchantments(Level level, ItemStack stack, boolean special) {
        int adjustedLevel = EnchantingHelper.getAdjustedLevel(stack, enchantingLevel);
        if (adjustedLevel == 0)
            return new ArrayList<>(0);
        var possible = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getTag(enchantmentTag)
                .stream()
                .flatMap(HolderSet::stream)
                .filter(target::isPrimaryItemFor);
        return EnchantingHelper.getAvailableEnchantmentResults(adjustedLevel, possible, special);
    }

    @Override
    public boolean canProcess(Level level, ItemStack stack, boolean special) {
        if (enchantments.isEmpty())
            return false;
        if (stack.getItem() instanceof EnchantingTemplateItem template) {
            if (stack.has(CEIDataComponents.ENCHANTMENT_INSTANCE))
                return false;
            return special || !template.isSpecial();
        }
        return false;
    }

    @Override
    public void update(Level level, ItemStack stack, int enchantingLevel, boolean special, boolean cursed) {
        super.update(level, target, enchantingLevel, special, cursed);
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, RandomSource random, boolean special, boolean struck) {
        var enchantments = EnchantingHelper.selectEnchantments(random, enchantingLevel, this.enchantments, special);
        if (enchantments.size() > 1) {
            enchantments.remove(random.nextInt(enchantments.size()));
        }
        if (struck) {
            var curses = getAvailableCurses(level, target);
            WeightedRandom.getRandomItem(random, curses).ifPresent(curse -> stack.enchant(curse.enchantment, curse.level));
        }
        for (EnchantmentInstance enchantmentinstance : enchantments) {
            stack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
        }
        return stack;
    }
}
