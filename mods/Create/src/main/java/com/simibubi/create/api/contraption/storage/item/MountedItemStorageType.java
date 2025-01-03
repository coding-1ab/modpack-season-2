package com.simibubi.create.api.contraption.storage.item;

import com.mojang.serialization.Codec;

import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MountedItemStorageType<T extends MountedItemStorage> {
	public static final Codec<MountedItemStorageType<?>> CODEC = ExtraCodecs.lazyInitializedCodec(
		() -> MountedStorageTypeRegistry.getItemsRegistry().getCodec()
	);

	public final Codec<T> codec;

	protected MountedItemStorageType(Codec<T> codec) {
		this.codec = codec;
	}

	@Nullable
	public abstract T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
