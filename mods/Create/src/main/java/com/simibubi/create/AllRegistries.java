package com.simibubi.create;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllRegistries {
	public static Supplier<IForgeRegistry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPES;
	public static Supplier<IForgeRegistry<FanProcessingType>> FAN_PROCESSING_TYPES;
	public static Supplier<IForgeRegistry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPES;

	public static final class Keys {
		public static final ResourceKey<Registry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPES = key("arm_interaction_point_types");
		public static final ResourceKey<Registry<FanProcessingType>> FAN_PROCESSING_TYPES = key("fan_processing_types");
		public static final ResourceKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPES = key("item_attribute_types");

		private static <T> ResourceKey<Registry<T>> key(String name) {
			return ResourceKey.createRegistryKey(Create.asResource(name));
		}
	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		ARM_INTERACTION_POINT_TYPES = event.create(new RegistryBuilder<ArmInteractionPointType>()
				.setName(Keys.ARM_INTERACTION_POINT_TYPES.location())
				.disableSaving());

		FAN_PROCESSING_TYPES = event.create(new RegistryBuilder<FanProcessingType>()
			.setName(Keys.FAN_PROCESSING_TYPES.location())
			.disableSaving());

		ITEM_ATTRIBUTE_TYPES = event.create(new RegistryBuilder<ItemAttributeType>()
			.setName(Keys.ITEM_ATTRIBUTE_TYPES.location())
			.disableSaving());
	}
}
