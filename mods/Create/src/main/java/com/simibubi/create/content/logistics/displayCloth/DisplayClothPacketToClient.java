package com.simibubi.create.content.logistics.displayCloth;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class DisplayClothPacketToClient extends DisplayClothPacket {

	public DisplayClothPacketToClient(int entityID, PackageOrder order, ItemStack costItem, int costAmount) {
		super(entityID, order, costItem, costAmount);
	}

	public DisplayClothPacketToClient(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	public boolean handle(Context context) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return true;
		if (!(level.getEntity(entityID) instanceof DisplayClothEntity dce))
			return true;
		
		dce.requestData.encodedRequest = order;
		dce.paymentItem = costItem;
		dce.paymentAmount = costAmount;
		return true;
	}

}
