package com.simibubi.create.impl.contraption.storage;

import java.util.Objects;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
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

		System.out.println("aaaaaaaaa");
		for (Object o : event.getForgeRegistry()) {
			System.out.println(o);
		}

		MountedStorageTypeRegistry.ITEMS_BY_BLOCK.register(Blocks.DISPENSER, AllMountedStorageTypes.DISPENSER.get());
		MountedStorageTypeRegistry.ITEMS_BY_BLOCK.register(Blocks.DROPPER, AllMountedStorageTypes.DISPENSER.get());
	}
}
