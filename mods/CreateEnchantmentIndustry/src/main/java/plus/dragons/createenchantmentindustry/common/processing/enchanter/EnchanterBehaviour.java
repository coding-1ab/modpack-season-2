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

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class EnchanterBehaviour extends ScrollValueBehaviour implements IHaveGoggleInformation {
    public static final BehaviourType<EnchanterBehaviour> TYPE = new BehaviourType<>();
    public static final String LEVEL = "EnchantingLevel";
    public static final String TEMPLATE = "EnchantingTemplate";
    private final BlazeEnchanterBlockEntity enchanter;
    private ItemStack template = ItemStack.EMPTY;
    protected List<Holder<Enchantment>> enchantments = ImmutableList.of();

    public EnchanterBehaviour(BlazeEnchanterBlockEntity enchanter, ValueBoxTransform transform) {
        super(CEILang.translate("gui.blaze_enchanter.level").component(), enchanter, transform);
        this.enchanter = enchanter;
        this.setValue(enchanter.getMaxEnchantLevel());
    }

    public ItemStack getTemplate() {
        return template;
    }

    public boolean setTemplate(ItemStack stack) {
        if (isValidTemplate(stack)) {
            template = stack.copy();
            updateEnchantments();
            blockEntity.setChanged();
            blockEntity.sendData();
            return true;
        }
        return false;
    }

    private boolean isValidTemplate(ItemStack stack) {
        return stack.isEmpty() ||
               (stack.isEnchantable() && enchanter.findPossibleEnchantments(stack).findAny().isPresent());
    }

    private void updateEnchantments() {
        if (template.isEmpty())
            enchantments = ImmutableList.of();
        else
            enchantments = enchanter.findPossibleEnchantments(template).toList();
    }

    @Override
    public void setValue(int value) {
        value = Mth.clamp(value, 0, enchanter.getMaxEnchantLevel());
        if (value == this.value)
            return;
        this.value = value;
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isSafeNBT() {
        return false;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        int max = enchanter.getMaxEnchantLevel();
        return new ValueSettingsBoard(
                label,
                max,
                max / 6,
                ImmutableList.of(Component.literal("Level")),
                new ValueSettingsFormatter(ValueSettings::format)
        );
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        var stack = player.getItemInHand(hand);
        if (AllItems.WRENCH.isIn(stack))
            return;
        if (AllBlocks.MECHANICAL_ARM.isIn(stack))
            return;
        var level = getWorld();
        var pos = getPos();
        if (stack.isEmpty()) {
            setTemplate(ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, .25f, .1f);
            return;
        }
        if (!setTemplate(stack.copy())) {
            player.displayClientMessage(CEILang.translate("gui.blaze_enchanter.template.invalid").component(), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    @Override
    public void write(CompoundTag nbt, Provider registries, boolean clientPacket) {
        nbt.putInt(LEVEL, value);
        nbt.put(TEMPLATE, template.saveOptional(registries));
    }

    @Override
    public void read(CompoundTag nbt, Provider registries, boolean clientPacket) {
        value = Math.clamp(nbt.getInt(LEVEL), 0, enchanter.getMaxEnchantLevel());
        template = ItemStack.parseOptional(registries, nbt.getCompound(TEMPLATE));
        if (getWorld() != null)
            updateEnchantments();
    }

    @Override
    public void initialize() {
        updateEnchantments();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = false;
        if (!template.isEmpty()) {
            CEILang.translate("gui.goggles.enchanting.template").forGoggles(tooltip);
            CEILang.item(template).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            added = true;
        }
        if (value > 0) {
            boolean isSuper = value > enchanter.getMaxEnchantLevel(false);
            var level = CEILang.number(value).style(isSuper ? ChatFormatting.BLUE : ChatFormatting.GOLD);
            CEILang.translate("gui.goggles.enchanting.level", level).forGoggles(tooltip);
            added = true;
        }
        return added;
    }
}
