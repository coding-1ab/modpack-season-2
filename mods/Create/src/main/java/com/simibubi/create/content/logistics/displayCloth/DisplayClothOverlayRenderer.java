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
import net.minecraft.world.phys.BlockHitResult;
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

		ItemStack heldItem = mc.player.getMainHandItem();

		if (mouseOver.getType() != Type.ENTITY) {
			if (!(mouseOver instanceof BlockHitResult bhr))
				return;
			if (!(mc.level.getBlockEntity(bhr.getBlockPos()) instanceof DisplayClothBlockEntity dcbe))
				return;
			if (!dcbe.isShop())
				return;

			int alreadyPurchased = 0;
			ShoppingList list = ShoppingListItem.getList(heldItem);
			if (list != null)
				alreadyPurchased = list.getPurchases(dcbe.getBlockPos());

			BlueprintOverlayRenderer.displayClothShop(dcbe, alreadyPurchased, list);
			return;
		}

		EntityHitResult entityRay = (EntityHitResult) mouseOver;
		if (!AllItems.SHOPPING_LIST.isIn(heldItem))
			return;

		ShoppingList list = ShoppingListItem.getList(heldItem);
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

}
