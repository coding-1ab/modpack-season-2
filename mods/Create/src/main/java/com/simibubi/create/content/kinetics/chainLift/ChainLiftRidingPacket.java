package com.simibubi.create.content.kinetics.chainLift;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ChainLiftRidingPacket extends BlockEntityConfigurationPacket<ChainLiftBlockEntity> {

	public ChainLiftRidingPacket(BlockPos pos) {
		super(pos);
	}
	
	public ChainLiftRidingPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void applySettings(ChainLiftBlockEntity be) {}

	@Override
	protected void applySettings(ServerPlayer sender, ChainLiftBlockEntity be) {
		sender.fallDistance = 0;
		sender.connection.aboveGroundTickCount = 0;
		sender.connection.aboveGroundVehicleTickCount = 0;
	}

}
