package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PackageOrderRequestPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {

	private PackageOrder order;
	private String address;

	public PackageOrderRequestPacket(BlockPos pos, PackageOrder order, String address) {
		super(pos);
		this.order = order;
		this.address = address;
	}

	public PackageOrderRequestPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeUtf(address);
		order.write(buffer);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		address = buffer.readUtf();
		order = PackageOrder.read(buffer);
	}

	@Override
	protected void applySettings(StockTickerBlockEntity be) {}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		be.receivePackageRequest(order, player, address);
	}

}
