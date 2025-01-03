package com.simibubi.create.content.contraptions.behaviour.dispenser.storage;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraftforge.items.IItemHandlerModifiable;

public class DispenserMountedStorageType extends SimpleMountedStorageType {
	public DispenserMountedStorageType() {
		super(DispenserMountedStorage.CODEC);
	}

	@Override
	protected SimpleMountedStorage createStorage(IItemHandlerModifiable handler) {
		return new DispenserMountedStorage(handler);
	}
}
