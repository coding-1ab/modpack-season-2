package com.simibubi.create.impl.contraption.storage;

import java.util.Objects;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MountedStorageTypeRegistryImpl {
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

	// low priority: want to run after the registration actually happens
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void registerDefaults(RegisterEvent event) {
		if (event.getRegistryKey() != MountedStorageTypeRegistry.ITEMS)
			return;

		register(Blocks.DISPENSER, AllMountedStorageTypes.DISPENSER);
		register(Blocks.DROPPER, AllMountedStorageTypes.DISPENSER);
	}

	private static void register(Block block, RegistryObject<? extends MountedItemStorageType<?>> type) {
		MountedStorageTypeRegistry.ITEM_LOOKUP.register(block, type.get());
	}

	public static MountedItemStorageType<?> itemFallback(Block block) {
		if (AllTags.AllBlockTags.CHEST_MOUNTED_STORAGE.matches(block)) {
			return AllMountedStorageTypes.CHEST.get();
		}if (AllTags.AllBlockTags.SIMPLE_MOUNTED_STORAGE.matches(block)) {
			return AllMountedStorageTypes.SIMPLE.get();
		} else if (!AllTags.AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST.matches(block)) {
			return AllMountedStorageTypes.FALLBACK.get();
		} else {
			return null;
		}
	}
}
