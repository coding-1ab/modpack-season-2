package com.simibubi.create;

import java.util.function.Supplier;

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

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllRegistries {
	public static Supplier<IForgeRegistry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPE;
	public static Supplier<IForgeRegistry<FanProcessingType>> FAN_PROCESSING_TYPE;
	public static Supplier<IForgeRegistry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPE;

	public static final class Keys {
		public static final ResourceKey<Registry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPE = key("arm_interaction_point_type");
		public static final ResourceKey<Registry<FanProcessingType>> FAN_PROCESSING_TYPE = key("fan_processing_type");
		public static final ResourceKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPE = key("item_attribute_type");

		private static <T> ResourceKey<Registry<T>> key(String name) {
			return ResourceKey.createRegistryKey(Create.asResource(name));
		}
	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		ARM_INTERACTION_POINT_TYPE = event.create(new RegistryBuilder<ArmInteractionPointType>()
			.setName(Keys.ARM_INTERACTION_POINT_TYPE.location())
			.disableSaving());

		FAN_PROCESSING_TYPE = event.create(new RegistryBuilder<FanProcessingType>()
			.setName(Keys.FAN_PROCESSING_TYPE.location())
			.disableSaving());

		ITEM_ATTRIBUTE_TYPE = event.create(new RegistryBuilder<ItemAttributeType>()
			.setName(Keys.ITEM_ATTRIBUTE_TYPE.location())
			.disableSaving());
	}
}
