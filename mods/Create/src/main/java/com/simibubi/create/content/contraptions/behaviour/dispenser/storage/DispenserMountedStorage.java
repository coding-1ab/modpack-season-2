package com.simibubi.create.content.contraptions.behaviour.dispenser.storage;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraftforge.items.IItemHandlerModifiable;

public class DispenserMountedStorage extends SimpleMountedStorage {
	public static final Codec<SimpleMountedStorage> CODEC = SimpleMountedStorage.codec(DispenserMountedStorage::new);

	protected DispenserMountedStorage(SimpleMountedStorageType type, IItemHandlerModifiable handler) {
		super(type, handler);
	}

	public DispenserMountedStorage(IItemHandlerModifiable handler) {
		this(AllMountedStorageTypes.DISPENSER.get(), handler);
	}

	@Override
	public boolean providesFuel() {
		return false;
	}
}
