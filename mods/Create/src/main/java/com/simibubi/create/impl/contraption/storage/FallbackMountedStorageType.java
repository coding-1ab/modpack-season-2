package com.simibubi.create.impl.contraption.storage;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A fallback mounted storage impl that will try to be used when no type is
 * registered for a block. This requires that the mounted block provide an item handler
 * whose class is exactly {@link ItemStackHandler}.
 */
public class FallbackMountedStorageType extends SimpleMountedStorageType {
	public static final Codec<SimpleMountedStorage> CODEC = SimpleMountedStorage.codec(
		handler -> new SimpleMountedStorage(AllMountedStorageTypes.FALLBACK.get(), handler)
	);

	public FallbackMountedStorageType() {
		super(CODEC);
	}

	@Override
	public Optional<IItemHandlerModifiable> validate(IItemHandler handler) {
		return handler.getClass() == ItemStackHandler.class
			? Optional.of((ItemStackHandler) handler)
			: Optional.empty();
	}
}
