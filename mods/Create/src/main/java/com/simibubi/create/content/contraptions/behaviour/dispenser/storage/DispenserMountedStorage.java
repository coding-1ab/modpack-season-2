package com.simibubi.create.content.contraptions.behaviour.dispenser.storage;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;

import net.minecraftforge.items.IItemHandler;

public class DispenserMountedStorage extends SimpleMountedStorage {
	public static final Codec<DispenserMountedStorage> CODEC = SimpleMountedStorage.codec(DispenserMountedStorage::new);

	protected DispenserMountedStorage(MountedItemStorageType<?> type, IItemHandler handler) {
		super(type, handler);
	}

	public DispenserMountedStorage(IItemHandler handler) {
		this(AllMountedStorageTypes.DISPENSER.get(), handler);
	}

	@Override
	public boolean providesFuel() {
		return false;
	}
}
