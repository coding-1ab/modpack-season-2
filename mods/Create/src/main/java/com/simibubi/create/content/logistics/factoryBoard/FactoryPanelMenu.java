package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FactoryPanelMenu extends GhostItemMenu<FactoryPanelBehaviour> {

	public FactoryPanelMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public FactoryPanelMenu(MenuType<?> type, int id, Inventory inv, FactoryPanelBehaviour contentHolder) {
		super(type, id, inv, contentHolder);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		ItemStackHandler itemStackHandler = new ItemStackHandler(1);
		if (contentHolder != null)
			itemStackHandler.setStackInSlot(0, contentHolder.getFilter()
				.copyWithCount(1));
		return itemStackHandler;
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	protected FactoryPanelBehaviour createOnClient(FriendlyByteBuf extraData) {
		return FactoryPanelBehaviour.at(Minecraft.getInstance().level, FactoryPanelPosition.receive(extraData));
	}

	@Override
	protected void addSlots() {
		addSlot(new SlotItemHandler(ghostInventory, 0, 16, 24));
		addPlayerSlots(0, 0);
	}

	@Override
	protected void saveData(FactoryPanelBehaviour contentHolder) {}

}
