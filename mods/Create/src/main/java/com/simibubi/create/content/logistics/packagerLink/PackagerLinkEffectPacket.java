package com.simibubi.create.content.logistics.packagerLink;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class PackagerLinkEffectPacket extends SimplePacketBase {

	private BlockPos pos;

	public PackagerLinkEffectPacket(BlockPos pos) {
		this.pos = pos;
	}

	public PackagerLinkEffectPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof PackagerLinkBlockEntity plbe)
				plbe.playEffect();
		});
		return true;
	}

}
