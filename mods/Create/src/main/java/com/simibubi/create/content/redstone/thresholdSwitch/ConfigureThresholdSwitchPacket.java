package com.simibubi.create.content.redstone.thresholdSwitch;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ConfigureThresholdSwitchPacket extends BlockEntityConfigurationPacket<ThresholdSwitchBlockEntity> {

	private int offBelow;
	private int onAbove;
	private boolean invert;

	public ConfigureThresholdSwitchPacket(BlockPos pos, int offBelow, int onAbove, boolean invert) {
		super(pos);
		this.offBelow = offBelow;
		this.onAbove = onAbove;
		this.invert = invert;
	}
	
	public ConfigureThresholdSwitchPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}
	
	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		offBelow = buffer.readInt();
		onAbove = buffer.readInt();
		invert = buffer.readBoolean();
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeInt(offBelow);
		buffer.writeInt(onAbove);
		buffer.writeBoolean(invert);
	}

	@Override
	protected void applySettings(ThresholdSwitchBlockEntity be) {
		be.offWhenBelow = offBelow;
		be.onWhenAbove = onAbove;
		be.setInverted(invert);
	}
	
}
