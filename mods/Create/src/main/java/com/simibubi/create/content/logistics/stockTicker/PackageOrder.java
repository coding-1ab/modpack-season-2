package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record PackageOrder(List<IntAttached<ItemStack>> stacks) {

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		for (IntAttached<ItemStack> entry : stacks) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putInt("Quantity", entry.getFirst());
			entryTag.put("Item", entry.getSecond()
				.serializeNBT());
			list.add(entryTag);
		}
		tag.put("Entries", list);
		return tag;
	}

	public static PackageOrder read(CompoundTag tag) {
		List<IntAttached<ItemStack>> stacks = new ArrayList<>();
		NBTHelper.iterateCompoundList(tag.getList("Entries", Tag.TAG_COMPOUND), entryTag -> stacks
			.add(IntAttached.with(entryTag.getInt("Quantity"), ItemStack.of(entryTag.getCompound("Item")))));
		return new PackageOrder(stacks);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(stacks.size());
		for (IntAttached<ItemStack> entry : stacks) {
			buffer.writeVarInt(entry.getFirst());
			buffer.writeItem(entry.getSecond());
		}
	}

	public static PackageOrder read(FriendlyByteBuf buffer) {
		int size = buffer.readVarInt();
		List<IntAttached<ItemStack>> stacks = new ArrayList<>();
		for (int i = 0; i < size; i++)
			stacks.add(IntAttached.with(buffer.readVarInt(), buffer.readItem()));
		return new PackageOrder(stacks);
	}

}
