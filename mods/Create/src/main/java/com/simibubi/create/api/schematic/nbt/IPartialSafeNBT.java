package com.simibubi.create.api.schematic.nbt;

import net.minecraft.nbt.CompoundTag;

public interface IPartialSafeNBT {
	/**
	 * This will always be called from the logical server
	 */
	void writeSafe(CompoundTag compound);
}
