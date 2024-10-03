package com.simibubi.create.content.logistics.displayCloth;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

public class DisplayClothPacketToServer extends DisplayClothPacket {

	public DisplayClothPacketToServer(int entityID, PackageOrder order, ItemStack costItem, int costAmount) {
		super(entityID, order, costItem, costAmount);
	}

	public DisplayClothPacketToServer(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	public boolean handle(Context context) {
		Level level = context.getSender()
			.level();
		if (level == null)
			return true;
		if (!(level.getEntity(entityID) instanceof DisplayClothEntity dce))
			return true;

		dce.requestData.encodedRequest = order;
		dce.paymentItem = costItem;
		dce.paymentAmount = costAmount;
		dce.sendData();
		return true;
	}

}
