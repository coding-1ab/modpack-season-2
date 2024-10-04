package com.simibubi.create.content.logistics.displayCloth;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.displayCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class DisplayClothOverlayRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		HitResult mouseOver = mc.hitResult;
		if (mouseOver == null)
			return;
		if (mouseOver.getType() != Type.ENTITY)
			return;

		EntityHitResult entityRay = (EntityHitResult) mouseOver;
		ItemStack shoppingListItem = mc.player.getMainHandItem();
		ShoppingList list = ShoppingListItem.getList(shoppingListItem);

		if (!(entityRay.getEntity() instanceof DisplayClothEntity dce)) {
			if (!AllItems.SHOPPING_LIST.isIn(shoppingListItem))
				return;
			BlockPos stockTickerPosition = StockTickerInteractionHandler.getStockTickerPosition(entityRay.getEntity());
			if (list == null || stockTickerPosition == null)
				return;
			if (!(mc.level.getBlockEntity(stockTickerPosition) instanceof StockTickerBlockEntity tickerBE))
				return;
			if (!tickerBE.behaviour.freqId.equals(list.shopNetwork()))
				return;

			BlueprintOverlayRenderer.displayShoppingList(list.bakeEntries(mc.level, null));
			return;
		}

		int alreadyPurchased = 0;
		if (list != null)
			alreadyPurchased = list.getPurchases(dce.getPosWithPixelY());

		BlueprintOverlayRenderer.displayClothShop(dce, alreadyPurchased, list);
	}

}
