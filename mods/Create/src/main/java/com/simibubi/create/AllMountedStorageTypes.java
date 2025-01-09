package com.simibubi.create;

import static com.simibubi.create.Create.REGISTRATE;

import java.util.function.Supplier;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.chest.ChestMountedStorageType;
import com.simibubi.create.content.contraptions.behaviour.dispenser.storage.DispenserMountedStorageType;
import com.simibubi.create.content.equipment.toolbox.ToolboxMountedStorageType;
import com.simibubi.create.content.logistics.crate.CreativeCrateMountedStorageType;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import com.simibubi.create.content.logistics.vault.ItemVaultMountedStorageType;
import com.simibubi.create.impl.contraption.storage.FallbackMountedStorageType;

import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.world.level.block.Blocks;

public class AllMountedStorageTypes {
	// fallback is special, provider is registered on lookup creation so it's always last
	public static final RegistryEntry<FallbackMountedStorageType> FALLBACK = simple("fallback", FallbackMountedStorageType::new);

	// registrations for these are handled by the blocks, not the types
	public static final RegistryEntry<CreativeCrateMountedStorageType> CREATIVE_CRATE = simple("creative_crate", CreativeCrateMountedStorageType::new);
	public static final RegistryEntry<ItemVaultMountedStorageType> VAULT = simple("vault", ItemVaultMountedStorageType::new);
	public static final RegistryEntry<ToolboxMountedStorageType> TOOLBOX = simple("toolbox", ToolboxMountedStorageType::new);

	// these are for external blocks, register associations here
	public static final RegistryEntry<SimpleMountedStorageType.Impl> SIMPLE = REGISTRATE.mountedItemStorage("simple", SimpleMountedStorageType.Impl::new)
		.registerTo(AllTags.AllBlockTags.SIMPLE_MOUNTED_STORAGE.tag)
		.register();
	public static final RegistryEntry<ChestMountedStorageType> CHEST = REGISTRATE.mountedItemStorage("chest", ChestMountedStorageType::new)
		.registerTo(AllTags.AllBlockTags.CHEST_MOUNTED_STORAGE.tag)
		.register();
	public static final RegistryEntry<DispenserMountedStorageType> DISPENSER = REGISTRATE.mountedItemStorage("dispenser", DispenserMountedStorageType::new)
		.registerTo(Blocks.DISPENSER)
		.registerTo(Blocks.DROPPER)
		.register();

	private static <T extends MountedItemStorageType<?>> RegistryEntry<T> simple(String name, Supplier<T> supplier) {
		return REGISTRATE.mountedItemStorage(name, supplier).register();
	}

	public static void register() {
	}
}
