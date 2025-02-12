package com.simibubi.create.api.contraption.storage.fluid;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MountedFluidStorageType<T extends MountedFluidStorage> {
	public static final Codec<MountedFluidStorageType<?>> CODEC = ExtraCodecs.lazyInitializedCodec(
		() -> MountedStorageTypeRegistries.getFluidsRegistry().getCodec()
	);

	public final Codec<? extends T> codec;

	protected MountedFluidStorageType(Codec<? extends T> codec) {
		this.codec = codec;
	}

	@Nullable
	public abstract T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
