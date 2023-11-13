package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class LogisticalStockResponsePacket extends SimplePacketBase {

	private BlockPos pos;
	private List<IntAttached<ItemStack>> items;
	private boolean lastPacket;

	public LogisticalStockResponsePacket(boolean lastPacket, BlockPos pos, List<IntAttached<ItemStack>> items) {
		this.lastPacket = lastPacket;
		this.pos = pos;
		this.items = items;
	}

	public LogisticalStockResponsePacket(FriendlyByteBuf buffer) {
		lastPacket = buffer.readBoolean();
		pos = buffer.readBlockPos();
		int count = buffer.readVarInt();
		items = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			items.add(IntAttached.with(buffer.readVarInt(), buffer.readItem()));
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(lastPacket);
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(items.size());
		for (IntAttached<ItemStack> entry : items) {
			buffer.writeVarInt(entry.getFirst());
			buffer.writeItem(entry.getSecond());
		}
	}

	@Override
	public boolean handle(Context context) {
		handleClient();
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void handleClient() {
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe)
			stbe.receiveStockPacket(items, lastPacket);
	}

}
