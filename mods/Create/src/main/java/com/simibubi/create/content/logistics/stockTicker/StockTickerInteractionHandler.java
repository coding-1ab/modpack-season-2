package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
		Entity rootVehicle = entity.getRootVehicle();
		if (!(rootVehicle instanceof SeatEntity))
			return;
		if (!(entity instanceof LivingEntity living))
			return;

		BlockPos pos = entity.blockPosition();
		int stations = 0;
		Level level = event.getLevel();
		BlockPos targetPos = null;

		for (Direction d : Iterate.horizontalDirections) {
			for (int y : Iterate.zeroAndOne) {
				BlockPos workstationPos = pos.relative(d)
					.above(y);
				if (!(level.getBlockState(workstationPos)
					.getBlock() instanceof LogisticalWorkstationBlock lw))
					continue;
				targetPos = workstationPos;
				stations++;
			}
		}
		
		if (stations != 1)
			return;
		
		final BlockPos posForUI = targetPos;
		if (level.isClientSide())
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> displayScreen(posForUI));

		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
		return;
	}

	@OnlyIn(Dist.CLIENT)
	private static void displayScreen(BlockPos tickerPos) {
		if (Minecraft.getInstance().level.getBlockEntity(tickerPos) instanceof StockTickerBlockEntity be)
			ScreenOpener.open(new StockTickerRequestScreen(be));
	}

}
