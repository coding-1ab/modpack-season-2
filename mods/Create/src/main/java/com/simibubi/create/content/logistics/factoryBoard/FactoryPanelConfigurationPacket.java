package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.createmod.catnip.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class FactoryPanelConfigurationPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {

	private Pointing side;
	private String address;

	public FactoryPanelConfigurationPacket(BlockPos pos, Pointing side, String address) {
		super(pos);
		this.side = side;
		this.address = address;
	}

	public FactoryPanelConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeVarInt(side.ordinal());
		buffer.writeUtf(address);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		side = Pointing.values()[buffer.readVarInt()];
		address = buffer.readUtf();
	}

	@Override
	protected void applySettings(FactoryPanelBlockEntity be) {
		FactoryPanelConnectionBehaviour behaviour = be.connections.get(side);
		if (behaviour != null) {
			behaviour.address = address;
			be.notifyUpdate();
		}
	}

}
