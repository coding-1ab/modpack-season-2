package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ChainConveyorConnectionHandler {

	private static BlockPos firstPos;
	private static ResourceKey<Level> firstDim;

	@SubscribeEvent
	public static void onItemUsedOnBlock(PlayerInteractEvent.RightClickBlock event) {
		ItemStack itemStack = event.getItemStack();
		BlockPos pos = event.getPos();
		Level level = event.getLevel();
		Player player = event.getEntity();
		BlockState blockState = level.getBlockState(pos);

		if (!AllBlocks.CHAIN_CONVEYOR.has(blockState))
			return;
		if (!itemStack.is(Items.CHAIN)) // Replace with tag? generic renderer?
			return;
		if (!player.mayBuild() || player instanceof FakePlayer)
			return;

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.CONSUME);

		if (!level.isClientSide())
			return;

		if (firstPos == null || firstDim != level.dimension()) {
			firstPos = pos;
			firstDim = level.dimension();
			CreateLang.text("First position selected") // TODO localisation entry
				.sendStatus(player);
			return;
		}

		// TODO max distance config

		ChainConveyorBlock chainConveyorBlock = AllBlocks.CHAIN_CONVEYOR.get();
		ChainConveyorBlockEntity sourceLift = chainConveyorBlock.getBlockEntity(level, firstPos);
		ChainConveyorBlockEntity targetLift = chainConveyorBlock.getBlockEntity(level, pos);

		if (sourceLift == null || targetLift == null) {
			firstPos = null;
			CreateLang.text("Connection failed") // TODO localisation entry
				.sendStatus(player);
			return;
		}

		AllPackets.getChannel()
			.sendToServer(new ChainConveyorConnectionPacket(firstPos, pos, true));

		CreateLang.text("Chain added") // TODO localisation entry
			.sendStatus(player);
		firstPos = null;
		firstDim = null;
	}

}
