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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Comparator;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.neoforged.neoforge.items.ItemStackHandler;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class BlazeForgerInventory extends ItemStackHandler {
    private final BlazeForgerBlockEntity forger;
    private int cost;

    public BlazeForgerInventory(BlazeForgerBlockEntity forger) {
        super(4);
        this.forger = forger;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public int getSlots() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack stack = super.getStackInSlot(slot);
        if (stack.isEmpty())
            stack = stacks.get(slot + 2);
        return stack;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stacks.get(slot + 2).isEmpty())
            return super.insertItem(slot, stack, simulate);
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (stacks.get(slot).isEmpty())
            return super.extractItem(slot + 2, amount, simulate);
        return ItemStack.EMPTY;
    }

    @Override
    protected void onLoad() {
        var level = forger.getLevel();
        if (level != null && !level.isClientSide)
            updateResult();
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (slot == 0 || slot == 1)
            updateResult();
        forger.notifyUpdate();
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        cost = nbt.getInt("Cost");
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        var nbt = super.serializeNBT(provider);
        nbt.putInt("Cost", cost);
        return nbt;
    }

    protected int getExperienceCost() {
        return cost == 0 ? 0 : ExperienceHelper.getExperienceForTotalLevel(cost);
    }

    protected ItemStack extractInput(int slot, boolean simulate) {
        validateSlotIndex(slot);
        ItemStack stack = stacks.get(slot);
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        if (!simulate)
            setStackInSlot(slot, ItemStack.EMPTY);
        return stack.copy();
    }

    protected ItemStack getResult(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot + 2);
    }

    protected void clearInput() {
        stacks.set(0, ItemStack.EMPTY);
        stacks.set(1, ItemStack.EMPTY);
        cost = 0;
    }

    protected void updateResult() {
        var base = stacks.get(0).copy();
        var addition = stacks.get(1).copy();
        stacks.set(2, base);
        stacks.set(3, addition);
        cost = 0;
        if (base.isEmpty() || addition.isEmpty())
            return;
        var baseType = EnchantmentHelper.getComponentType(base);
        var baseEnchantments = base.getOrDefault(baseType, ItemEnchantments.EMPTY);
        var additionType = EnchantmentHelper.getComponentType(addition);
        var additionEnchantments = addition.getOrDefault(additionType, ItemEnchantments.EMPTY);
        if (baseType == DataComponents.STORED_ENCHANTMENTS) {
            if (addition.getItem() instanceof EnchantingTemplateItem template) {
                if (forger.special && !template.isSpecial()) return;
                if (additionEnchantments.isEmpty()) {
                    if (!splitEnchantments(base, addition, baseEnchantments, additionEnchantments)) return;
                } else {
                    if (applyEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                        stacks.set(3, ItemStack.EMPTY);
                    } else return;
                }
            } else if (additionType == DataComponents.STORED_ENCHANTMENTS) {
                if (applyEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                    stacks.set(3, ItemStack.EMPTY);
                } else return;
            } else return;
        } else {
            if (addition.getItem() instanceof EnchantingTemplateItem template) {
                if (forger.special && !template.isSpecial()) return;
                if (additionEnchantments.isEmpty()) {
                    if (!splitEnchantments(base, addition, baseEnchantments, additionEnchantments)) return;
                } else {
                    if (applyEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                        stacks.set(3, ItemStack.EMPTY);
                    } else return;
                }
            } else if (additionType == DataComponents.STORED_ENCHANTMENTS) {
                if (applyEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                    stacks.set(3, ItemStack.EMPTY);
                } else return;
            } else if (ItemStack.isSameItem(base, addition)) {
                if (combineEnchantments(base, addition, baseEnchantments, additionEnchantments)) {
                    stacks.set(3, ItemStack.EMPTY);
                } else return;
            } else return;
        }
        applyRepairCost(base, addition);
    }

    protected boolean splitEnchantments(ItemStack base, ItemStack addition, ItemEnchantments baseEnchantments, ItemEnchantments additionEnchantments) {
        if (baseEnchantments.isEmpty())
            return false;
        var registry = Objects.requireNonNull(forger.getLevel()).registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var stream = baseEnchantments.keySet().stream().sorted(Comparator.comparingInt(holder -> registry.getId(holder.value())));
        if (!forger.special) {
            stream = stream.filter(holder -> !holder.is(EnchantmentTags.CURSE));
        }
        var optional = stream.findFirst();
        if (optional.isEmpty())
            return false;
        var enchantment = optional.get();
        var removedEnchantments = new ItemEnchantments.Mutable(baseEnchantments);
        removedEnchantments.set(enchantment, 0);
        EnchantmentHelper.setEnchantments(base, removedEnchantments.toImmutable());
        int level = baseEnchantments.getLevel(enchantment);
        if (!forger.special)
            level = Math.min(level, enchantment.value().getMaxLevel());
        addition.enchant(enchantment, level);
        cost += Math.max(1, enchantment.value().getAnvilCost() * 2) * level;
        return true;
    }

    protected boolean applyEnchantments(ItemStack base, ItemStack addition, ItemEnchantments baseEnchantments, ItemEnchantments additionEnchantments) {
        int cost = 0;
        var resultEnchantments = new Mutable(baseEnchantments);
        boolean applied = false;
        for (Entry<Holder<Enchantment>> entry : additionEnchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int baseLevel = resultEnchantments.getLevel(holder);
            int additionLevel = entry.getIntValue();
            int resultLevel = baseLevel == additionLevel ? additionLevel + 1 : Math.max(additionLevel, baseLevel);
            Enchantment enchantment = holder.value();
            boolean applicable = base.supportsEnchantment(holder);
            for (Holder<Enchantment> holder1 : resultEnchantments.keySet()) {
                if (!holder1.equals(holder) && !Enchantment.areCompatible(holder, holder1)) {
                    applicable = forger.special && CEIConfig.enchantments().ignoreEnchantmentCompatibility.get();
                    cost++;
                }
            }

            if (applicable) {
                applied = true;
                int maxLevel = enchantment.getMaxLevel();
                int extendedMaxLevel = maxLevel + CEIConfig.enchantments().enchantmentMaxLevelExtension.get();

                if (resultLevel > extendedMaxLevel) {
                    resultLevel = extendedMaxLevel;
                } else if (resultLevel > maxLevel && !forger.special) {
                    resultLevel = maxLevel;
                }

                resultEnchantments.set(holder, resultLevel);
                int anvilCost = enchantment.getAnvilCost();
                anvilCost = Math.max(1, anvilCost / 2);

                cost += anvilCost * resultLevel;
            }
        }
        if (!applied)
            return false;
        EnchantmentHelper.setEnchantments(base, resultEnchantments.toImmutable());
        this.cost += cost;
        return true;
    }

    protected boolean combineEnchantments(ItemStack base, ItemStack addition, ItemEnchantments baseEnchantments, ItemEnchantments additionEnchantments) {
        boolean applied = false;
        if (base.isDamaged()) {
            int baseDurability = base.getMaxDamage() - base.getDamageValue();
            int additionDurability = addition.getMaxDamage() - addition.getDamageValue();
            int fix = additionDurability + base.getMaxDamage() * 12 / 100;
            int resultDurability = baseDurability + fix;
            int resultDamage = base.getMaxDamage() - resultDurability;
            if (resultDamage < 0) {
                resultDamage = 0;
            }

            if (resultDamage < base.getDamageValue()) {
                base.setDamageValue(resultDamage);
                cost += 2;
                applied = true;
            }
        }
        applied |= applyEnchantments(base, addition, baseEnchantments, additionEnchantments);
        return applied;
    }

    protected void applyRepairCost(ItemStack base, ItemStack addition) {
        if (!forger.cursed)
            return;
        int baseCost = base.getOrDefault(DataComponents.REPAIR_COST, 0);
        int additionCost = addition.getOrDefault(DataComponents.REPAIR_COST, 0);
        int resultCost = AnvilMenu.calculateIncreasedRepairCost(Math.max(baseCost, additionCost));
        base.set(DataComponents.REPAIR_COST, resultCost);
    }
}
