package com.simibubi.create.impl.contraption.storage;

import java.util.Objects;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import com.simibubi.create.api.lookup.BlockLookup;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraft.world.level.block.Block;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MountedStorageTypeRegistryImpl {
	public static final BlockLookup<MountedItemStorageType<?>> ITEM_LOOKUP = BlockLookup.create(MountedStorageTypeRegistryImpl::itemFallback);

	private static IForgeRegistry<MountedItemStorageType<?>> itemsRegistry;

	public static IForgeRegistry<MountedItemStorageType<?>> getItemsRegistry() {
		return Objects.requireNonNull(itemsRegistry, "Registry accessed too early");
	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		event.create(
			new RegistryBuilder<MountedItemStorageType<?>>()
				.setName(MountedStorageTypeRegistry.ITEMS.location()),
			registry -> itemsRegistry = registry
		);
	}

	public static MountedItemStorageType<?> itemFallback(Block block) {
		return AllTags.AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST.matches(block)
			? null
			: AllMountedStorageTypes.FALLBACK.get();
	}
}
