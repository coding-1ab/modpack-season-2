package com.simibubi.create.api.schematic.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.ApiStatus;

public interface IPartialSafeNBT {
	/** This will always be called from the logical server */
	void writeSafe(CompoundTag compound);
}
