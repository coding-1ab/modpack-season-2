package com.simibubi.create.compat.storageDrawers;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public class StorageDrawers {

	public static boolean isDrawer(BlockEntity be) {
		return be != null && Mods.STORAGEDRAWERS.id()
			.equals(CatnipServices.REGISTRIES.getKeyOrThrow(be.getType())
				.getNamespace());
	}

	public static int getTotalStorageSpace(IItemHandler inv) {
		int totalSpace = 0;
		for (int slot = 1; slot < inv.getSlots(); slot++)
			totalSpace += inv.getSlotLimit(slot);
		return totalSpace;
	}

	public static int getItemCount(IItemHandler inv, FilteringBehaviour filtering) {
		int occupied = 0;
		for (int slot = 1; slot < inv.getSlots(); slot++) {
			ItemStack stackInSlot = inv.getStackInSlot(slot);
			int space = inv.getSlotLimit(slot);
			int count = stackInSlot.getCount();
			if (space == 0)
				continue;
			if (filtering.test(stackInSlot))
				occupied += count;
		}
		return occupied;
	}

}
