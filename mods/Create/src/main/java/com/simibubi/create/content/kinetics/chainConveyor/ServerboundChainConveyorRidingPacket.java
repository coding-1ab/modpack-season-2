package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ServerboundChainConveyorRidingPacket extends BlockEntityConfigurationPacket<ChainConveyorBlockEntity> {

	public ServerboundChainConveyorRidingPacket(BlockPos pos) {
		super(pos);
	}

	public ServerboundChainConveyorRidingPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {}

	@Override
	protected void applySettings(ChainConveyorBlockEntity be) {}

	@Override
	protected void applySettings(ServerPlayer sender, ChainConveyorBlockEntity be) {
		sender.fallDistance = 0;
		sender.connection.aboveGroundTickCount = 0;
		sender.connection.aboveGroundVehicleTickCount = 0;
		ServerChainConveyorHandler.handleTTLPacket(sender);
	}

}
