package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StockTickerBlockEntity extends LogisticalWorkstationBlockEntity {

	protected List<IntAttached<ItemStack>> lastClientsideStockSnapshot;
	protected List<IntAttached<ItemStack>> newlyReceivedStockSnapshot;

	public StockTickerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	public void refreshClientStockSnapshot() {
		AllPackets.getChannel()
			.sendToServer(new LogisticalStockRequestPacket(worldPosition));
	}

	public List<IntAttached<ItemStack>> getClientStockSnapshot() {
		return lastClientsideStockSnapshot;
	}

	public void receiveStockPacket(List<IntAttached<ItemStack>> stacks, boolean endOfTransmission) {
		if (newlyReceivedStockSnapshot == null)
			newlyReceivedStockSnapshot = new ArrayList<>();
		newlyReceivedStockSnapshot.addAll(stacks);
		if (!endOfTransmission)
			return;
		lastClientsideStockSnapshot = newlyReceivedStockSnapshot;
		newlyReceivedStockSnapshot = null;
	}

}
