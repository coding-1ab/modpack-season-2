package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.logistics.displayCloth.ShoppingListItem;
import com.simibubi.create.content.logistics.displayCloth.ShoppingListItem.ShoppingList;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.utility.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class StockTickerInteractionHandler {

	@SubscribeEvent
	public static void interactWithLogisticsManager(EntityInteractSpecific event) {
		Entity entity = event.getTarget();
		Player player = event.getEntity();
		if (player == null || entity == null)
			return;
		if (player.isSpectator())
			return;

		Level level = event.getLevel();
		BlockPos targetPos = getStockTickerPosition(entity);
		if (targetPos == null)
			return;

		ItemStack mainHandItem = player.getMainHandItem();
		if (AllItems.SHOPPING_LIST.isIn(mainHandItem)) {

			if (!level.isClientSide()) {
				if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity tickerBE))
					return;
				ShoppingList list = ShoppingListItem.getList(mainHandItem);
				if (list == null)
					return;

				PackageOrder order = new PackageOrder(list.bakeEntries(level)
					.getFirst()
					.getStacksByCount());

				tickerBE.broadcastPackageRequest(order, null, ShoppingListItem.getAddress(mainHandItem));
				player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			}

			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
			return;
		}

		final BlockPos posForUI = targetPos;
		final boolean encodeMode =
			AllItems.DISPLAY_CLOTH.isIn(mainHandItem) || AllBlocks.REDSTONE_REQUESTER.isIn(mainHandItem);

		if (level.isClientSide())
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> displayScreen(posForUI, encodeMode));

		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
		return;
	}

	public static BlockPos getStockTickerPosition(Entity entity) {
		Entity rootVehicle = entity.getRootVehicle();
		if (!(rootVehicle instanceof SeatEntity))
			return null;
		if (!(entity instanceof LivingEntity living))
			return null;

		BlockPos pos = entity.blockPosition();
		int stations = 0;
		BlockPos targetPos = null;

		for (Direction d : Iterate.horizontalDirections) {
			for (int y : Iterate.zeroAndOne) {
				BlockPos workstationPos = pos.relative(d)
					.above(y);
				if (!(entity.level()
					.getBlockState(workstationPos)
					.getBlock() instanceof StockTickerBlock lw))
					continue;
				targetPos = workstationPos;
				stations++;
			}
		}

		if (stations != 1)
			return null;
		return targetPos;
	}

	@OnlyIn(Dist.CLIENT)
	private static void displayScreen(BlockPos tickerPos, boolean encodeRequester) {
		if (Minecraft.getInstance().level.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity be)
			ScreenOpener.open(new StockTickerRequestScreen(be, encodeRequester));
	}

}
