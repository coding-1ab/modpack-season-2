package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.IntAttached;
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
		previouslyUsedAddress = "";
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

	public void receivePackageRequest(PackageOrder order, Player player, String address) {
		List<IntAttached<ItemStack>> stacks = order.stacks();

		// Packages need to track their index and successors for successful defrag
		List<PackagerLinkBlockEntity> availableLinks = getAvailableLinks();
		List<PackagerLinkBlockEntity> usedLinks = new ArrayList<>();
		MutableBoolean finalLinkTracker = new MutableBoolean(false);

		// First box needs to carry the order specifics for successful defrag
		PackageOrder contextToSend = order;

		// Packages from future orders should not be merged in the packager queue
		int orderId = Create.RANDOM.nextInt();

		for (int i = 0; i < stacks.size(); i++) {
			IntAttached<ItemStack> entry = stacks.get(i);
			int remainingCount = entry.getFirst();
			boolean finalEntry = i == stacks.size() - 1;
			ItemStack requestedItem = entry.getSecond();

			for (PackagerLinkBlockEntity link : availableLinks) {
				int usedIndex = usedLinks.indexOf(link);
				int linkIndex = usedIndex == -1 ? usedLinks.size() : usedIndex;
				MutableBoolean isFinalLink = new MutableBoolean(false);
				if (linkIndex == usedLinks.size() - 1)
					isFinalLink = finalLinkTracker;

				int processedCount = link.processRequest(requestedItem, remainingCount, address, linkIndex, isFinalLink,
					orderId, contextToSend);
				if (processedCount > 0 && usedIndex == -1) {
					contextToSend = null;
					usedLinks.add(link);
					finalLinkTracker = isFinalLink;
				}
				remainingCount -= processedCount;
				if (remainingCount > 0)
					continue;
				if (finalEntry)
					finalLinkTracker.setTrue();
				break;
			}
		}

		previouslyUsedAddress = address;
		notifyUpdate();
	}

}
