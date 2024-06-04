package com.simibubi.create.content.kinetics.chainLift;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ChainLiftConnectionPacket extends BlockEntityConfigurationPacket<ChainLiftBlockEntity> {

	private BlockPos targetPos;
	private boolean connect;

	public ChainLiftConnectionPacket(BlockPos pos, BlockPos targetPos, boolean connect) {
		super(pos);
		this.targetPos = targetPos;
		this.connect = connect;
	}

	public ChainLiftConnectionPacket(FriendlyByteBuf buffer) {
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
	protected void applySettings(ChainLiftBlockEntity be) {
		// TODO max size config check

		if (!(be.getLevel()
			.getBlockEntity(targetPos) instanceof ChainLiftBlockEntity clbe))
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
