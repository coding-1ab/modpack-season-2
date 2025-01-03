package com.simibubi.create.api.contraption.storage.item.simple;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;

import com.simibubi.create.foundation.utility.CreateCodecs;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Widely-applicable mounted storage implementation.
 * Gets an item handler from the mounted block, copies it to an ItemStackHandler,
 * and then copies the inventory back to the target when unmounting.
 * All blocks for which this mounted storage is registered must provide an
 * {@link IItemHandlerModifiable} to {@link ForgeCapabilities#ITEM_HANDLER}.
 * <br>
 * To use this implementation, either register {@link AllMountedStorageTypes#SIMPLE} to your block
 * manually, or add your block to the {@link AllTags.AllBlockTags#SIMPLE_MOUNTED_STORAGE} tag.
 */
public class SimpleMountedStorage extends WrapperMountedItemStorage<ItemStackHandler> {
	public static final Codec<SimpleMountedStorage> CODEC = codec(SimpleMountedStorage::new);

	public SimpleMountedStorage(SimpleMountedStorageType type, IItemHandlerModifiable handler) {
		super(type, copyToItemStackHandler(handler));
	}

	public SimpleMountedStorage(IItemHandlerModifiable handler) {
		this(AllMountedStorageTypes.SIMPLE.get(), handler);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be == null)
			return;

		be.getCapability(ForgeCapabilities.ITEM_HANDLER)
			.resolve()
			.filter(handler -> handler.getSlots() == this.getSlots())
			.flatMap(this::validate)
			.ifPresent(handler -> {
				for (int i = 0; i < handler.getSlots(); i++) {
					handler.setStackInSlot(i, this.getStackInSlot(i));
				}
			});
	}

	private Optional<IItemHandlerModifiable> validate(IItemHandler handler) {
		return ((SimpleMountedStorageType) this.type).validate(handler);
	}

	public static Codec<SimpleMountedStorage> codec(Function<IItemHandlerModifiable, SimpleMountedStorage> factory) {
		return CreateCodecs.ITEM_STACK_HANDLER.xmap(
			factory, storage -> storage.wrapped
		);
	}
}
