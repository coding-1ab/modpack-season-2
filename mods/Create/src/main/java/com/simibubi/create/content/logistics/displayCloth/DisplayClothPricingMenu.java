package com.simibubi.create.content.logistics.displayCloth;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class DisplayClothPricingMenu extends GhostItemMenu<DisplayClothEntity> {

	int pricingAmount;

	public DisplayClothPricingMenu(MenuType<?> type, int id, Inventory inv, DisplayClothEntity contentHolder) {
		super(type, id, inv, contentHolder);
		pricingAmount = contentHolder.paymentAmount;
	}

	public DisplayClothPricingMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public static DisplayClothPricingMenu create(int id, Inventory inv, DisplayClothEntity contentHolder) {
		return new DisplayClothPricingMenu(AllMenuTypes.DISPLAY_CLOTH.get(), id, inv, contentHolder);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		ItemStackHandler handler = new ItemStackHandler(1);
		handler.setStackInSlot(0, contentHolder.paymentItem.isEmpty() ? contentHolder.paymentItem
			: contentHolder.paymentItem.copyWithCount(1));
		return handler;
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	protected DisplayClothEntity createOnClient(FriendlyByteBuf extraData) {
		int entityID = extraData.readVarInt();
		PackageOrder order = PackageOrder.read(extraData);
		Entity entityByID = Minecraft.getInstance().level.getEntity(entityID);
		if (!(entityByID instanceof DisplayClothEntity dce))
			return null;
		dce.requestData.encodedRequest = order;
		pricingAmount = dce.paymentAmount;
		return dce;
	}

	@Override
	protected void addSlots() {
		int playerX = 33;
		int playerY = 118;
		int slotX = 0;
		int slotY = 70;

		addPlayerSlots(playerX, playerY);
		addSlot(new SlotItemHandler(ghostInventory, 0, slotX, slotY));
	}

	@Override
	protected void saveData(DisplayClothEntity contentHolder) {
		if (!contentHolder.level()
			.isClientSide())
			return;

		contentHolder.paymentItem = ghostInventory.getStackInSlot(0);
		contentHolder.paymentAmount = pricingAmount;

		AllPackets.getChannel()
			.sendToServer(new DisplayClothPacketToServer(contentHolder.getId(),
				contentHolder.requestData.encodedRequest, contentHolder.paymentItem, contentHolder.paymentAmount));
	}

}
