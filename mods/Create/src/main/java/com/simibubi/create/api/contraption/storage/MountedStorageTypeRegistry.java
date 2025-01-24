package com.simibubi.create.api.contraption.storage;

import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.lookup.BlockLookup;
import com.simibubi.create.impl.contraption.storage.MountedStorageTypeRegistryImpl;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraftforge.registries.IForgeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class MountedStorageTypeRegistry {
	public static final ResourceKey<Registry<MountedItemStorageType<?>>> ITEMS = ResourceKey.createRegistryKey(
		Create.asResource("mounted_item_storage_type")
	);

	/**
	 * Lookup used for finding the item storage type associated with a block.
	 * @see BlockLookup
	 */
	public static final BlockLookup<MountedItemStorageType<?>> ITEM_LOOKUP = MountedStorageTypeRegistryImpl.ITEM_LOOKUP;

	/**
	 * @throws NullPointerException if called before registry registration
	 */
	public static IForgeRegistry<MountedItemStorageType<?>> getItemsRegistry() {
		return MountedStorageTypeRegistryImpl.getItemsRegistry();
	}

	/**
	 * Utility for use with Registrate builders. Creates a builder transformer
	 * that will register the given MountedItemStorageType to a block when ready.
	 */
	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mountedItemStorage(RegistryEntry<? extends MountedItemStorageType<?>> type) {
		return builder -> builder.onRegisterAfter(ITEMS, block -> ITEM_LOOKUP.register(block, type.get()));
	}
}
