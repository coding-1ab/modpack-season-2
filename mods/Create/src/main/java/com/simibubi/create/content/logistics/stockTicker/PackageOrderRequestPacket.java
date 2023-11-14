package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PackageOrderRequestPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {

	private List<IntAttached<ItemStack>> items;
	private String address;

	public PackageOrderRequestPacket(BlockPos pos, List<IntAttached<ItemStack>> items, String address) {
		super(pos);
		this.items = items;
		this.address = address;
	}

	public PackageOrderRequestPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeUtf(address);
		buffer.writeVarInt(items.size());
		for (IntAttached<ItemStack> entry : items) {
			buffer.writeVarInt(entry.getFirst());
			buffer.writeItem(entry.getSecond());
		}
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		address = buffer.readUtf();
		int size = buffer.readVarInt();
		items = new ArrayList<>();
		for (int i = 0; i < size; i++)
			items.add(IntAttached.with(buffer.readVarInt(), buffer.readItem()));
	}

	@Override
	protected void applySettings(StockTickerBlockEntity be) {}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		be.receivePackageRequest(items, player, address);
	}

}
