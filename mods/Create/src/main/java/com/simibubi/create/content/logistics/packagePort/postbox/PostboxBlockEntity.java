package com.simibubi.create.content.logistics.packagePort.postbox;

import java.lang.ref.WeakReference;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.GlobalStation.GlobalPackagePort;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PostboxBlockEntity extends PackagePortBlockEntity {

	public WeakReference<GlobalStation> trackedGlobalStation;

	public PostboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		trackedGlobalStation = new WeakReference<GlobalStation>(null);
	}

	@Override
	public void onChunkUnloaded() {
		if (level == null || level.isClientSide)
			return;
		GlobalStation station = trackedGlobalStation.get();
		if (station == null)
			return;
		if (!station.connectedPorts.containsKey(worldPosition))
			return;
		GlobalPackagePort globalPackagePort = station.connectedPorts.get(worldPosition);
		for (int i = 0; i < inventory.getSlots(); i++) {
			globalPackagePort.offlineBuffer.setStackInSlot(i, inventory.getStackInSlot(i));
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		}

		globalPackagePort.primed = true;
		Create.RAILWAYS.markTracksDirty();
		super.onChunkUnloaded();
	}

}
