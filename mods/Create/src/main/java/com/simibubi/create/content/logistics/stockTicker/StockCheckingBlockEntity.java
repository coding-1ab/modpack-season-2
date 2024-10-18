package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public abstract class StockCheckingBlockEntity extends SmartBlockEntity {

	public LogisticallyLinkedBehaviour behaviour;

	public StockCheckingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this));
	}

	public InventorySummary getRecentSummary() {
		return behaviour.getSummaryOfNetwork(false);
	}

	public InventorySummary getAccurateSummary() {
		return behaviour.getSummaryOfNetwork(true);
	}

	public void broadcastPackageRequest(PackageOrder order, IItemHandler ignoredHandler, String address) {
		List<BigItemStack> stacks = order.stacks();

		// Packages need to track their index and successors for successful defrag
		Iterable<LogisticallyLinkedBehaviour> availableLinks = behaviour.getAllConnectedAvailableLinks(true);
		List<LogisticallyLinkedBehaviour> usedLinks = new ArrayList<>();
		MutableBoolean finalLinkTracker = new MutableBoolean(false);

		// First box needs to carry the order specifics for successful defrag
		PackageOrder contextToSend = order;

		// Packages from future orders should not be merged in the packager queue
		int orderId = level.random.nextInt();

		for (int i = 0; i < stacks.size(); i++) {
			BigItemStack entry = stacks.get(i);
			int remainingCount = entry.count;
			boolean finalEntry = i == stacks.size() - 1;
			ItemStack requestedItem = entry.stack;

			for (LogisticallyLinkedBehaviour link : availableLinks) {
				int usedIndex = usedLinks.indexOf(link);
				int linkIndex = usedIndex == -1 ? usedLinks.size() : usedIndex;
				MutableBoolean isFinalLink = new MutableBoolean(false);
				if (linkIndex == usedLinks.size() - 1)
					isFinalLink = finalLinkTracker;

				int processedCount = link.processRequest(requestedItem, remainingCount, address, linkIndex, isFinalLink,
					orderId, contextToSend, ignoredHandler);
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
	}

}
