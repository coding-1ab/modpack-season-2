package com.simibubi.create.api.contraption.storage;

import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import com.simibubi.create.impl.contraption.storage.MountedStorageTypeRegistryImpl;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class MountedStorageTypeRegistry {
	public static final ResourceKey<Registry<MountedItemStorageType<?>>> ITEMS = ResourceKey.createRegistryKey(
		Create.asResource("mounted_item_storage_type")
	);

	public static final AttachedRegistry<Block, MountedItemStorageType<?>> ITEMS_BY_BLOCK = new AttachedRegistry<>(ForgeRegistries.BLOCKS);

	public static IForgeRegistry<MountedItemStorageType<?>> getItemsRegistry() {
		return MountedStorageTypeRegistryImpl.getItemsRegistry();
	}
}
