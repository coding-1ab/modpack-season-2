package com.simibubi.create.content.logistics.packager;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public record PackagingRequest(ItemStack item, MutableInt count, String address) {

	public static PackagingRequest create(ItemStack item, int count, String address) {
		return new PackagingRequest(item, new MutableInt(count), address);
	}

	public static PackagingRequest fromNBT(CompoundTag tag) {
		return create(ItemStack.of(tag.getCompound("Item")), tag.getInt("Count"), tag.getString("Address"));
	}

	public int getCount() {
		return count.intValue();
	}

	public void subtract(int toSubtract) {
		count.setValue(getCount() - toSubtract);
	}

	public boolean isEmpty() {
		return getCount() == 0;
	}

	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Count", count.intValue());
		tag.put("Item", item.serializeNBT());
		tag.putString("Address", address);
		return tag;
	}

}
