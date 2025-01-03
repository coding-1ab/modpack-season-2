package com.simibubi.create.api.contraption.storage.item;

import com.google.common.collect.ImmutableMap;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import net.minecraft.core.BlockPos;

public class MountedItemStorageWrapper extends CombinedInvWrapper {
	public final ImmutableMap<BlockPos, MountedItemStorage> storages;

	public MountedItemStorageWrapper(ImmutableMap<BlockPos, MountedItemStorage> storages) {
		super(storages.values().toArray(IItemHandlerModifiable[]::new));
		this.storages = storages;
	}
}
