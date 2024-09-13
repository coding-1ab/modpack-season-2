package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.createmod.catnip.utility.IntAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class StockTickerConfigurationPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {

	private boolean takeSnapshot;
	private String address;
	private List<IntAttached<ItemStack>> amounts;

	public StockTickerConfigurationPacket(BlockPos pos, boolean takeSnapshot, String address,
		List<IntAttached<ItemStack>> amounts) {
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
		buffer.writeVarInt(amounts.size());
		for (IntAttached<ItemStack> intAttached : amounts) {
			buffer.writeVarInt(intAttached.getFirst());
			buffer.writeItem(intAttached.getValue());
		}
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		takeSnapshot = buffer.readBoolean();
		address = buffer.readUtf();
		int items = buffer.readVarInt();
		amounts = new ArrayList<>();
		for (int i = 0; i < items; i++)
			amounts.add(IntAttached.with(buffer.readVarInt(), buffer.readItem()));
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
