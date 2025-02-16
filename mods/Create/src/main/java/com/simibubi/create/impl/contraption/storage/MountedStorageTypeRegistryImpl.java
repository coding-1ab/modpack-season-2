package com.simibubi.create.impl.contraption.storage;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistries;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.Util;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

@ApiStatus.Internal
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MountedStorageTypeRegistryImpl {
	public static final SimpleRegistry<Block, MountedItemStorageType<?>> ITEM_STORAGES = Util.make(() -> {
		SimpleRegistry<Block, MountedItemStorageType<?>> registry = SimpleRegistry.create();
		registry.registerProvider(ItemFallbackProvider.INSTANCE);
		return registry;
	});
	public static final SimpleRegistry<Block, MountedFluidStorageType<?>> FLUID_STORAGES = SimpleRegistry.create();

	private static IForgeRegistry<MountedItemStorageType<?>> itemsRegistry;
	private static IForgeRegistry<MountedFluidStorageType<?>> fluidsRegistry;

	public static IForgeRegistry<MountedItemStorageType<?>> getItemsRegistry() {
		return Objects.requireNonNull(itemsRegistry, "Registry accessed too early");
	}

	public static IForgeRegistry<MountedFluidStorageType<?>> getFluidsRegistry() {
		return Objects.requireNonNull(fluidsRegistry, "Registry accessed too early");
	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		event.create(
			new RegistryBuilder<MountedItemStorageType<?>>()
				.setName(MountedStorageTypeRegistries.ITEMS.location()),
			registry -> itemsRegistry = registry
		);
		event.create(
			new RegistryBuilder<MountedFluidStorageType<?>>()
				.setName(MountedStorageTypeRegistries.FLUIDS.location()),
			registry -> fluidsRegistry = registry
		);
	}

	private enum ItemFallbackProvider implements SimpleRegistry.Provider<Block, MountedItemStorageType<?>> {
		INSTANCE;

		@Override
		@Nullable
		public MountedItemStorageType<?> get(Block block) {
			return AllTags.AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST.matches(block)
				? null
				: AllMountedStorageTypes.FALLBACK.get();
		}

		@Override
		public void onRegister(SimpleRegistry<Block, MountedItemStorageType<?>> registry) {
			MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
				if (event.shouldUpdateStaticData()) {
					registry.invalidate();
				}
			});
		}
	}
}
