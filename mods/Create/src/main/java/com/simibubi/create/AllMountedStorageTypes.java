package com.simibubi.create;

import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.chest.ChestMountedStorageType;
import com.simibubi.create.content.contraptions.behaviour.dispenser.storage.DispenserMountedStorageType;
import com.simibubi.create.content.equipment.toolbox.ToolboxMountedStorageType;
import com.simibubi.create.content.logistics.crate.CreativeCrateMountedStorageType;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import com.simibubi.create.content.logistics.vault.ItemVaultMountedStorageType;
import com.simibubi.create.impl.contraption.storage.FallbackMountedStorageType;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AllMountedStorageTypes {
	private static final DeferredRegister<MountedItemStorageType<?>> REGISTER = DeferredRegister.create(MountedStorageTypeRegistry.ITEMS, Create.ID);

	public static final RegistryObject<SimpleMountedStorageType> SIMPLE = REGISTER.register("simple", SimpleMountedStorageType::new);
	public static final RegistryObject<FallbackMountedStorageType> FALLBACK = REGISTER.register("fallback", FallbackMountedStorageType::new);
	public static final RegistryObject<ChestMountedStorageType> CHEST = REGISTER.register("chest", ChestMountedStorageType::new);
	public static final RegistryObject<DispenserMountedStorageType> DISPENSER = REGISTER.register("dispenser", DispenserMountedStorageType::new);
	public static final RegistryObject<CreativeCrateMountedStorageType> CREATIVE_CRATE = REGISTER.register("creative_crate", CreativeCrateMountedStorageType::new);
	public static final RegistryObject<ItemVaultMountedStorageType> VAULT = REGISTER.register("vault", ItemVaultMountedStorageType::new);
	public static final RegistryObject<ToolboxMountedStorageType> TOOLBOX = REGISTER.register("toolbox", ToolboxMountedStorageType::new);

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
