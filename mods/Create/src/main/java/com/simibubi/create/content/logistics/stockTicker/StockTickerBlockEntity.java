package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StockTickerBlockEntity extends LogisticalWorkstationBlockEntity {

	protected List<IntAttached<ItemStack>> lastClientsideStockSnapshot;
	protected List<IntAttached<ItemStack>> newlyReceivedStockSnapshot;
	
	protected String previouslyUsedAddress;

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
	
	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putString("PreviousAddress", previouslyUsedAddress);
	}
	
	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		previouslyUsedAddress = tag.getString("PreviousAddress");
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
	
	public void receivePackageRequest(List<IntAttached<ItemStack>> stacks, Player player, String address) {
		List<PackagerLinkBlockEntity> availableLinks = getAvailableLinks();
		for (IntAttached<ItemStack> entry : stacks) {
			int remainingCount = entry.getFirst();
			ItemStack requestedItem = entry.getSecond();
			for (PackagerLinkBlockEntity link : availableLinks) {
				remainingCount -= link.processRequest(requestedItem, remainingCount, address);
				if (remainingCount == 0)
					break;
			}
		}
		previouslyUsedAddress = address;
		notifyUpdate();
	}

}
