package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class StockTickerConfigurationPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {

	private boolean takeSnapshot;
	private String address;
	private PackageOrder amounts;

	public StockTickerConfigurationPacket(BlockPos pos, boolean takeSnapshot, String address, PackageOrder amounts) {
		super(pos);
		this.takeSnapshot = takeSnapshot;
		this.address = address;
		this.amounts = amounts;
	}

	public StockTickerConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBoolean(takeSnapshot);
		buffer.writeUtf(address);
		amounts.write(buffer);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		takeSnapshot = buffer.readBoolean();
		address = buffer.readUtf();
		amounts = PackageOrder.read(buffer);
	}

	@Override
	protected void applySettings(StockTickerBlockEntity be) {
		if (takeSnapshot) {
			be.takeInventoryStockSnapshot();
			return;
		}

		be.updateAutoRestockSettings(address, amounts);
	}

}
