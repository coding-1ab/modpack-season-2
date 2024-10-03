package com.simibubi.create.content.logistics.displayCloth;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public abstract class DisplayClothPacket extends SimplePacketBase {

	protected int entityID;
	protected PackageOrder order;
	protected ItemStack costItem;
	protected int costAmount;

	public DisplayClothPacket(int entityID, PackageOrder order, ItemStack costItem, int costAmount) {
		this.entityID = entityID;
		this.order = order;
		this.costItem = costItem;
		this.costAmount = costAmount;
	}
	
	public DisplayClothPacket(FriendlyByteBuf buffer) {
		this(buffer.readVarInt(), PackageOrder.read(buffer), buffer.readItem(), buffer.readVarInt());
	}
	
	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(entityID);
		order.write(buffer);
		buffer.writeItem(costItem);
		buffer.writeVarInt(costAmount);
	}

}
