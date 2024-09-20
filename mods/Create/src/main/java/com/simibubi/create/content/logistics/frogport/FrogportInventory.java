package com.simibubi.create.content.logistics.frogport;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.box.PackageItem;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class FrogportInventory extends ItemStackHandler {

	private boolean receiveMode;
	private FrogportBlockEntity port;

	public FrogportInventory(FrogportBlockEntity port) {
		super(9);
		this.port = port;
		receiveMode = false;
	}

	public void receiveMode(boolean enable) {
		receiveMode = enable;
	}

	public boolean isBackedUp() {
		for (int i = 0; i < getSlots(); i++)
			if (getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	public boolean isEmpty() {
		for (int i = 0; i < getSlots(); i++)
			if (!getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (!(stack.getItem() instanceof PackageItem))
			return stack;
		if (receiveMode)
			return super.insertItem(slot, stack, simulate);

		if (port.isAnimationInProgress())
			return stack;
		if (port.target == null || !port.target.export(port.getLevel(), port.getBlockPos(), stack, true))
			return stack;
		if (!simulate)
			port.startAnimation(stack.copy(), true);
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack extractItem = super.extractItem(slot, amount, simulate);
		if (!simulate && !extractItem.isEmpty())
			port.notifyUpdate();
		return extractItem;
	}

}
