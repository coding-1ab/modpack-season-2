package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.network.FriendlyByteBuf;

public class FactoryPanelConfigurationPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {

	private PanelSlot slot;
	private String address;

	public FactoryPanelConfigurationPacket(FactoryPanelPosition position, String address) {
		super(position.pos());
		this.slot = position.slot();
		this.address = address;
	}

	public FactoryPanelConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeVarInt(slot.ordinal());
		buffer.writeUtf(address);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		slot = PanelSlot.values()[buffer.readVarInt()];
		address = buffer.readUtf();
	}

	@Override
	protected void applySettings(FactoryPanelBlockEntity be) {
		FactoryPanelBehaviour behaviour = be.panels.get(slot);
		if (behaviour != null) {
			behaviour.recipeAddress = address;
			be.notifyUpdate();
		}
	}

}
