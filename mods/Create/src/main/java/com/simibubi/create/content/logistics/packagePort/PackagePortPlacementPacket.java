package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class PackagePortPlacementPacket extends SimplePacketBase {

	private PackagePortTarget target;
	private BlockPos pos;

	public PackagePortPlacementPacket(PackagePortTarget target, BlockPos pos) {
		this.target = target;
		this.pos = pos;
	}

	public PackagePortPlacementPacket(FriendlyByteBuf buffer) {
		target = PackagePortTarget.read(buffer.readNbt());
		pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(target.write());
		buffer.writeBlockPos(pos);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null)
				return;
			Level world = player.level();
			if (world == null || !world.isLoaded(pos))
				return;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!(blockEntity instanceof PackagePortBlockEntity ppbe))
				return;
			target.setup(ppbe, world, pos);
			ppbe.target = target;
			ppbe.notifyUpdate();
		});
		return true;
	}

	public static class ClientBoundRequest extends SimplePacketBase {

		BlockPos pos;

		public ClientBoundRequest(BlockPos pos) {
			this.pos = pos;
		}

		public ClientBoundRequest(FriendlyByteBuf buffer) {
			this.pos = buffer.readBlockPos();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeBlockPos(pos);
		}

		@Override
		public boolean handle(Context context) {
			context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> PackagePortTargetSelectionHandler.flushSettings(pos)));
			return true;
		}

	}

}
