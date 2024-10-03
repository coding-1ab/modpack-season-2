package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneRequesterBlockEntity extends SmartBlockEntity {

	protected AutoRequestData requestData = new AutoRequestData();
	protected boolean redstonePowered;

	public RedstoneRequesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	protected void onRedstonePowerChanged() {
		boolean hasNeighborSignal = level.hasNeighborSignal(worldPosition);
		if (redstonePowered == hasNeighborSignal)
			return;

		if (hasNeighborSignal)
			requestData.triggerRequest(level, worldPosition);

		redstonePowered = hasNeighborSignal;
		setChanged();
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		redstonePowered = tag.getBoolean("Powered");
		requestData = AutoRequestData.read(tag);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putBoolean("Powered", redstonePowered);
		requestData.write(tag);
	}

}
