package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.createmod.catnip.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class FactoryPanelConnectionPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {

	private BlockPos fromPos;
	private Pointing fromSide;
	private Pointing toSide;

	public FactoryPanelConnectionPacket(BlockPos fromPos, Pointing fromSide, BlockPos toPos, Pointing toSide) {
		super(toPos);
		this.fromPos = fromPos;
		this.fromSide = fromSide;
		this.toSide = toSide;
	}

	public FactoryPanelConnectionPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(fromPos);
		buffer.writeVarInt(fromSide.ordinal());
		buffer.writeVarInt(toSide.ordinal());
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		fromPos = buffer.readBlockPos();
		fromSide = Pointing.values()[buffer.readVarInt()];
		toSide = Pointing.values()[buffer.readVarInt()];
	}

	@Override
	protected void applySettings(FactoryPanelBlockEntity be) {
		be.addConnection(fromPos, fromSide, toSide);
	}

	@Override
	protected int maxRange() {
		return super.maxRange() * 2;
	}

}
