package com.simibubi.create.content.fluids.tank.storage;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.utility.CreateCodecs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidTankMountedStorage extends WrapperMountedFluidStorage<FluidTank> {
	public static final Codec<FluidTankMountedStorage> CODEC = CreateCodecs.FLUID_TANK.xmap(
		FluidTankMountedStorage::new, storage -> storage.wrapped
	);

	protected FluidTankMountedStorage(MountedFluidStorageType<?> type, FluidTank wrapped) {
		super(type, wrapped);
	}

	protected FluidTankMountedStorage(FluidTank wrapped) {
		this(AllMountedStorageTypes.FLUID_TANK.get(), wrapped);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be instanceof FluidTankBlockEntity tank && tank.isController()) {
			FluidTank inventory = tank.getTankInventory();
			// capacity shouldn't change, leave it
			inventory.setFluid(this.wrapped.getFluid());
		}
	}

	public FluidTank getTank() {
		return this.wrapped;
	}

	public static FluidTankMountedStorage fromTank(FluidTankBlockEntity tank) {
		// tank has update callbacks, make an isolated copy
		FluidTank inventory = tank.getTankInventory();
		FluidTank copy = new FluidTank(inventory.getCapacity());
		copy.setFluid(inventory.getFluid());
		return new FluidTankMountedStorage(copy);
	}

	public static FluidTankMountedStorage fromLegacy(CompoundTag nbt) {
		int capacity = nbt.getInt("Capacity");
		FluidTank tank = new FluidTank(capacity);
		tank.readFromNBT(nbt);
		return new FluidTankMountedStorage(tank);
	}
}
