package com.simibubi.create.content.contraptions.behaviour.dispenser.storage;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;

import com.simibubi.create.api.contraption.storage.item.menu.MountedStorageMenus;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DispenserMountedStorage extends SimpleMountedStorage {
	public static final Codec<DispenserMountedStorage> CODEC = SimpleMountedStorage.codec(DispenserMountedStorage::new);

	protected DispenserMountedStorage(MountedItemStorageType<?> type, IItemHandler handler) {
		super(type, handler);
	}

	public DispenserMountedStorage(IItemHandler handler) {
		this(AllMountedStorageTypes.DISPENSER.get(), handler);
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	@Nullable
	protected MenuProvider createMenuProvider(Component name, IItemHandlerModifiable handler,
											  Predicate<Player> stillValid, Consumer<Player> onClose) {
		return MountedStorageMenus.createGeneric9x9(name, handler, stillValid, onClose);
	}

	@Override
	protected void playOpeningSound(Level level, Vec3 pos) {
		// dispensers are silent
	}
}
