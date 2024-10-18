package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.createmod.catnip.utility.Pointing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class FactoryPanelEffectPacket extends SimplePacketBase {

	private BlockPos fromPos;
	private Pointing fromSide;
	private Pointing toSide;
	private BlockPos toPos;
	private boolean success;

	public FactoryPanelEffectPacket(BlockPos fromPos, Pointing fromSide, BlockPos toPos, Pointing toSide,
		boolean success) {
		this.fromPos = fromPos;
		this.fromSide = fromSide;
		this.toPos = toPos;
		this.toSide = toSide;
		this.success = success;
	}

	public FactoryPanelEffectPacket(FriendlyByteBuf buffer) {
		toPos = buffer.readBlockPos();
		fromPos = buffer.readBlockPos();
		fromSide = Pointing.values()[buffer.readVarInt()];
		toSide = Pointing.values()[buffer.readVarInt()];
		success = buffer.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(toPos);
		buffer.writeBlockPos(fromPos);
		buffer.writeVarInt(fromSide.ordinal());
		buffer.writeVarInt(toSide.ordinal());
		buffer.writeBoolean(success);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean handle(Context context) {
		ClientLevel level = Minecraft.getInstance().level;
		BlockState blockState = level.getBlockState(fromPos);
		if (!AllBlocks.FACTORY_PANEL.has(blockState))
			return true;
		FactoryPanelRenderer.renderConnection(blockState, fromPos, toPos, fromSide, toSide, success, true);
		return true;
	}
}
