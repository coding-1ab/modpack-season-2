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

import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;

public abstract class BlazeExperienceBlock<T extends BlazeExperienceBlockEntity> extends BlazeBlock<T> {
    public BlazeExperienceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        T blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        boolean notConsume = player.hasInfiniteMaterials();
        if (stack.is(AllItems.CREATIVE_BLAZE_CAKE)) {
            blockEntity.applyCreativeFuel();
            if (!notConsume)
                stack.shrink(1);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        boolean forceOverflow = !(player instanceof FakePlayer);
        var resultHolder = applyFuel(level, blockEntity, stack, forceOverflow, notConsume, false);
        var result = resultHolder.getResult();
        if (result == InteractionResult.PASS)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var remainder = resultHolder.getObject();
        if (!remainder.isEmpty()) {
            if (stack.isEmpty())
                player.setItemInHand(hand, remainder);
            else
                player.getInventory().placeItemBackInInventory(remainder);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public InteractionResultHolder<ItemStack> applyFuel(Level level, T blockEntity, ItemStack stack, boolean forceOverflow, boolean notConsume, boolean simulate) {
        var fuel = ExperienceFuel.get(level, stack);
        if (fuel != null) {
            boolean applied = blockEntity.applyExperienceFuel(fuel, forceOverflow, simulate);
            if (applied) {
                if (!simulate && !notConsume)
                    stack.shrink(1);
                ItemStack remainder = notConsume
                        ? ItemStack.EMPTY
                        : fuel.usingConvertTo().orElse(stack.getCraftingRemainingItem());
                return InteractionResultHolder.success(remainder);
            }
        }
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }
}
