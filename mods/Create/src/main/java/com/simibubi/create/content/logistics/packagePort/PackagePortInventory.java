package com.simibubi.create.content.logistics.packagePort;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.box.PackageItem;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class PackagePortInventory extends ItemStackHandler {

	private boolean receiveMode;
	private PackagePortBlockEntity port;

	public PackagePortInventory(PackagePortBlockEntity port) {
		super(9);
		this.port = port;
		receiveMode = false;
	}

	public void receiveMode(boolean enable) {
		receiveMode = enable;
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (!(stack.getItem() instanceof PackageItem))
			return stack;
		if (receiveMode)
			return super.insertItem(slot, stack, simulate);

		PackagePortTarget target = port.target;
		if (target == null)
			return stack;
		if (!target.export(port.getLevel(), port.getBlockPos(), stack, simulate))
			return stack;

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
