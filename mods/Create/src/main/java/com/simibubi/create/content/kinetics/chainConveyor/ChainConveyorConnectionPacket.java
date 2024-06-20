package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ChainConveyorConnectionPacket extends BlockEntityConfigurationPacket<ChainConveyorBlockEntity> {

	private BlockPos targetPos;
	private boolean connect;

	public ChainConveyorConnectionPacket(BlockPos pos, BlockPos targetPos, boolean connect) {
		super(pos);
		this.targetPos = targetPos;
		this.connect = connect;
	}

	public ChainConveyorConnectionPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(targetPos);
		buffer.writeBoolean(connect);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		targetPos = buffer.readBlockPos();
		connect = buffer.readBoolean();
	}

	@Override
	protected void applySettings(ChainConveyorBlockEntity be) {
		// TODO max size config check

		if (!(be.getLevel()
			.getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity clbe))
			return;

		if (connect) {
			if (!clbe.addConnectionTo(be.getBlockPos()))
				return;
		} else
			clbe.removeConnectionTo(be.getBlockPos());

		if (connect) {
			if (!be.addConnectionTo(targetPos))
				clbe.removeConnectionTo(be.getBlockPos());
		} else
			be.removeConnectionTo(targetPos);
	}

}
