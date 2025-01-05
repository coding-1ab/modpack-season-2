package com.simibubi.create.content.contraptions.behaviour.dispenser;

import java.util.function.Predicate;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.item.ItemHelper;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraftforge.items.IItemHandler;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;

public class DropperMovementBehaviour implements MovementBehaviour {
	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (context.world.isClientSide || context.getStorage() == null)
			return;

		int slot = getSlot(context.getStorage(), context.world.random);
		if (slot == -1) {
			// all slots empty
			failDispense(context, pos);
			return;
		}

		// copy because dispense behaviors will modify it directly
		ItemStack stack = context.getStorage().getStackInSlot(slot).copy();
		if (stack.getCount() == 1 && stack.getMaxStackSize() != 1) {
			// last one, try to top it off
			IItemHandler contraptionInventory = context.contraption.getStorage().getAllItems();
			if (!tryTopOff(stack, contraptionInventory)) {
				// failed, abort dispense to preserve filters
				failDispense(context, pos);
				return;
			}
		}

		IMovedDispenseItemBehaviour behavior = getDispenseBehavior(context, pos, stack);
		ItemStack remainder = behavior.dispense(stack, context, pos);
		context.getStorage().setStackInSlot(slot, remainder);
	}

	protected IMovedDispenseItemBehaviour getDispenseBehavior(MovementContext context, BlockPos pos, ItemStack stack) {
		return MovedDefaultDispenseItemBehaviour.INSTANCE;
	}

	private static boolean tryTopOff(ItemStack stack, IItemHandler from) {
		Predicate<ItemStack> test = otherStack -> ItemStack.isSameItemSameTags(stack, otherStack);
		int needed = stack.getMaxStackSize() - stack.getCount();

		ItemStack extracted = ItemHelper.extract(from, test, ItemHelper.ExtractionCountMode.UPTO, needed, false);
		if (!extracted.isEmpty()) {
			stack.grow(extracted.getCount());
			return true;
		}

		return false;
	}

	private static int getSlot(MountedItemStorage storage, RandomSource random) {
		IntList filledSlots = new IntArrayList();
		for (int i = 0; i < storage.getSlots(); i++) {
			ItemStack stack = storage.getStackInSlot(i);
			if (!stack.isEmpty()) {
				filledSlots.add(i);
			}
		}

		return switch (filledSlots.size()) {
			case 0 -> -1;
			case 1 -> filledSlots.getInt(0);
			default -> Util.getRandom(filledSlots, random);
		};
	}

	private static void failDispense(MovementContext ctx, BlockPos pos) {
		ctx.world.levelEvent(LevelEvent.SOUND_DISPENSER_FAIL, pos, 0);
	}
}
