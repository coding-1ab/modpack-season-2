package com.simibubi.create.content.logistics.factoryBoard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.network.FriendlyByteBuf;

public class FactoryPanelConfigurationPacket extends BlockEntityConfigurationPacket<FactoryPanelBlockEntity> {

	private PanelSlot slot;
	private String address;
	private Map<FactoryPanelPosition, Integer> inputAmounts;
	private int outputAmount;
	private int promiseClearingInterval;
	private FactoryPanelPosition removeConnection;
	private boolean clearPromises;

	public FactoryPanelConfigurationPacket(FactoryPanelPosition position, String address,
		Map<FactoryPanelPosition, Integer> inputAmounts, int outputAmount, int promiseClearingInterval,
		@Nullable FactoryPanelPosition removeConnection, boolean clearPromises) {
		super(position.pos());
		this.address = address;
		this.inputAmounts = inputAmounts;
		this.outputAmount = outputAmount;
		this.promiseClearingInterval = promiseClearingInterval;
		this.removeConnection = removeConnection;
		this.clearPromises = clearPromises;
		this.slot = position.slot();
	}

	public FactoryPanelConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeVarInt(slot.ordinal());
		buffer.writeUtf(address);
		buffer.writeVarInt(inputAmounts.size());
		for (Entry<FactoryPanelPosition, Integer> entry : inputAmounts.entrySet()) {
			entry.getKey()
				.send(buffer);
			buffer.writeVarInt(entry.getValue());
		}
		buffer.writeVarInt(outputAmount);
		buffer.writeVarInt(promiseClearingInterval);
		buffer.writeBoolean(removeConnection != null);
		if (removeConnection != null)
			removeConnection.send(buffer);
		buffer.writeBoolean(clearPromises);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		slot = PanelSlot.values()[buffer.readVarInt()];
		address = buffer.readUtf();
		inputAmounts = new HashMap<>();
		int entries = buffer.readVarInt();
		for (int i = 0; i < entries; i++)
			inputAmounts.put(FactoryPanelPosition.receive(buffer), buffer.readVarInt());
		outputAmount = buffer.readVarInt();
		promiseClearingInterval = buffer.readVarInt();
		if (buffer.readBoolean())
			removeConnection = FactoryPanelPosition.receive(buffer);
		clearPromises = buffer.readBoolean();
	}

	@Override
	protected void applySettings(FactoryPanelBlockEntity be) {
		FactoryPanelBehaviour behaviour = be.panels.get(slot);
		if (behaviour == null)
			return;

		behaviour.recipeAddress = address;

		for (Entry<FactoryPanelPosition, Integer> entry : inputAmounts.entrySet()) {
			FactoryPanelPosition key = entry.getKey();
			FactoryPanelConnection connection = behaviour.targetedBy.get(key);
			if (connection != null)
				behaviour.targetedBy.put(key, new FactoryPanelConnection(key, entry.getValue()));
		}

		behaviour.recipeOutput = outputAmount;
		behaviour.promiseClearingInterval = promiseClearingInterval;

		if (removeConnection != null) {
			behaviour.targetedBy.remove(removeConnection);
			FactoryPanelBehaviour source = FactoryPanelBehaviour.at(be.getLevel(), removeConnection);
			if (source != null) {
				source.targeting.remove(behaviour.getPanelPosition());
				source.blockEntity.sendData();
			}
		}

		if (clearPromises)
			behaviour.forceClearPromises = true;

		be.notifyUpdate();
	}

}
