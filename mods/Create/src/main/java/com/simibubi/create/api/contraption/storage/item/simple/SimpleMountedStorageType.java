package com.simibubi.create.api.contraption.storage.item.simple;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.minecraftforge.common.capabilities.ForgeCapabilities;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleMountedStorageType extends MountedItemStorageType<SimpleMountedStorage> {
	protected SimpleMountedStorageType(Codec<SimpleMountedStorage> codec) {
		super(codec);
	}

	public SimpleMountedStorageType() {
		this(SimpleMountedStorage.CODEC);
	}

	@Override
	@Nullable
	public SimpleMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return Optional.ofNullable(be)
			.map(b -> b.getCapability(ForgeCapabilities.ITEM_HANDLER))
			.flatMap(LazyOptional::resolve)
			.flatMap(this::validate)
			.map(this::createStorage)
			.orElse(null);
	}

	public Optional<IItemHandlerModifiable> validate(IItemHandler handler) {
		return handler instanceof IItemHandlerModifiable modifiable
			? Optional.of(modifiable)
			: Optional.empty();
	}

	protected SimpleMountedStorage createStorage(IItemHandlerModifiable handler) {
		return new SimpleMountedStorage(this, handler);
	}
}
