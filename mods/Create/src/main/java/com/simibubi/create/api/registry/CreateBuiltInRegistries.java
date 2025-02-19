package com.simibubi.create.api.registry;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.serialization.Lifecycle;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.mixin.accessor.BuiltInRegistriesAccessor;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;

/**
 * Static registries added by Create.
 * @see CreateRegistries
 */
public class CreateBuiltInRegistries {
	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = simple(CreateRegistries.ARM_INTERACTION_POINT_TYPE);
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPE = simple(CreateRegistries.FAN_PROCESSING_TYPE);
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = simple(CreateRegistries.ITEM_ATTRIBUTE_TYPE);
	public static final Registry<DisplaySource> DISPLAY_SOURCE = simple(CreateRegistries.DISPLAY_SOURCE);
	public static final Registry<DisplayTarget> DISPLAY_TARGET = simple(CreateRegistries.DISPLAY_TARGET);
	public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPE = simple(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE);
	public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPE = simple(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE);

	private static <T> Registry<T> simple(ResourceKey<Registry<T>> key) {
		return register(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
	}

	@SuppressWarnings("unchecked")
	private static <T> Registry<T> register(ResourceKey<Registry<T>> key, WritableRegistry<T> registry) {
		BuiltInRegistriesAccessor.getWRITABLE_REGISTRY().register(
			(ResourceKey<WritableRegistry<?>>) (Object) key, registry, Lifecycle.stable()
		);
		return registry;
	}

	@ApiStatus.Internal
	public static void init() {
		// make sure the class is loaded.
	}
}
