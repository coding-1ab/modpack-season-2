package com.simibubi.create.content.logistics.displayCloth;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class DisplayClothConfigurationPacket extends BlockEntityConfigurationPacket<DisplayClothBlockEntity> {

	protected PackageOrder order;
	protected ItemStack costItem;
	protected int costAmount;

	public DisplayClothConfigurationPacket(BlockPos pos, PackageOrder order, ItemStack costItem, int costAmount) {
		super(pos);
		this.order = order;
		this.costItem = costItem;
		this.costAmount = costAmount;
	}

	public DisplayClothConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		order.write(buffer);
		buffer.writeItem(costItem);
		buffer.writeVarInt(costAmount);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		order = PackageOrder.read(buffer);
		costItem = buffer.readItem();
		costAmount = buffer.readVarInt();
	}

	@Override
	protected void applySettings(DisplayClothBlockEntity be) {
		be.requestData.encodedRequest = order;
		be.paymentItem = costItem;
		be.paymentAmount = costAmount;
		be.notifyUpdate();
	}

}
