package com.simibubi.create.api.contraption.storage.item.chest;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraftforge.items.IItemHandlerModifiable;

public class ChestMountedStorageType extends SimpleMountedStorageType {
	public ChestMountedStorageType() {
		super(ChestMountedStorage.CODEC);
	}

	@Override
	protected SimpleMountedStorage createStorage(IItemHandlerModifiable handler) {
		return new ChestMountedStorage(handler);
	}
}
