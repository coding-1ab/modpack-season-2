package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;

import net.createmod.catnip.utility.BlockFace;
import net.createmod.catnip.utility.IntAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public class StockTickerBlockEntity extends StockCheckingBlockEntity {

	// Player-interface Feature
	protected List<IntAttached<ItemStack>> lastClientsideStockSnapshot;
	protected List<IntAttached<ItemStack>> newlyReceivedStockSnapshot;
	protected String previouslyUsedAddress;
	protected int activeLinks;

	// Auto-restock Feature
	protected InvManipulationBehaviour observedInventory;
	protected PackageOrder restockAmounts;
	protected String restockAddress;
	protected boolean powered;

	public StockTickerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		previouslyUsedAddress = "";
		restockAddress = "";
		restockAmounts = PackageOrder.empty();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours
			.add(observedInventory = new InvManipulationBehaviour(this, (w, p, s) -> new BlockFace(p, Direction.DOWN)));
	}

	public void refreshClientStockSnapshot() {
		AllPackets.getChannel()
			.sendToServer(new LogisticalStockRequestPacket(worldPosition));
	}

	public List<IntAttached<ItemStack>> getClientStockSnapshot() {
		return lastClientsideStockSnapshot;
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide())
			return;
		if (activeLinks != activeLinksLastSummary && !isRemoved()) {
			activeLinks = activeLinksLastSummary;
			sendData();
		}
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putString("PreviousAddress", previouslyUsedAddress);
		tag.put("RestockAmounts", restockAmounts.write());
		tag.putString("RestockAddress", restockAddress);
		tag.putBoolean("Powered", powered);

		if (clientPacket)
			tag.putInt("ActiveLinks", activeLinks);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		previouslyUsedAddress = tag.getString("PreviousAddress");
		restockAmounts = PackageOrder.read(tag.getCompound("RestockAmounts"));
		restockAddress = tag.getString("RestockAddress");
		powered = tag.getBoolean("Powered");

		if (clientPacket)
			activeLinks = tag.getInt("ActiveLinks");
	}

	protected void takeInventoryStockSnapshot() {
		restockAmounts = PackageOrder.empty();
		IItemHandler inventory = observedInventory.getInventory();
		if (inventory == null)
			return;
		restockAmounts = new PackageOrder(summariseObservedInventory().getStacksByCount());
		List<IntAttached<ItemStack>> stacks = restockAmounts.stacks();
		if (stacks.size() > 8)
			stacks.subList(8, stacks.size())
				.clear();
		notifyUpdate();
	}

	private InventorySummary summariseObservedInventory() {
		IItemHandler inventory = observedInventory.getInventory();
		if (inventory == null)
			return InventorySummary.EMPTY;
		InventorySummary inventorySummary = new InventorySummary();
		for (int i = 0; i < inventory.getSlots(); i++)
			inventorySummary.add(inventory.getStackInSlot(i));
		return inventorySummary;
	}

	protected void onRedstonePowerChanged() {
		boolean hasNeighborSignal = level.hasNeighborSignal(worldPosition);
		if (powered == hasNeighborSignal)
			return;

		if (hasNeighborSignal)
			triggerRestock();

		powered = hasNeighborSignal;
		setChanged();
	}

	protected void triggerRestock() {
		if (!observedInventory.hasInventory() || restockAmounts.isEmpty())
			return;

		InventorySummary presentStock = summariseObservedInventory();
		List<IntAttached<ItemStack>> missingItems = new ArrayList<>();
		for (IntAttached<ItemStack> required : restockAmounts.stacks()) {
			int diff = required.getFirst() - presentStock.getCountOf(required.getValue());
			if (diff > 0)
				missingItems.add(IntAttached.with(diff, required.getValue()));
		}

		if (missingItems.isEmpty())
			return;

		broadcastPackageRequest(new PackageOrder(missingItems), observedInventory.getInventory(), restockAddress);
	}

	protected void updateAutoRestockSettings(String address, PackageOrder amounts) {
		restockAmounts = amounts;
		restockAddress = address;
		notifyUpdate();
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

	public void broadcastPackageRequest(PackageOrder order, IItemHandler ignoredHandler, String address) {
		List<IntAttached<ItemStack>> stacks = order.stacks();

		// Packages need to track their index and successors for successful defrag
		List<LogisticallyLinkedBehaviour> availableLinks = behaviour.getAllConnectedAvailableLinks(true);
		List<LogisticallyLinkedBehaviour> usedLinks = new ArrayList<>();
		MutableBoolean finalLinkTracker = new MutableBoolean(false);

		// First box needs to carry the order specifics for successful defrag
		PackageOrder contextToSend = order;

		// Packages from future orders should not be merged in the packager queue
		int orderId = level.random.nextInt();

		for (int i = 0; i < stacks.size(); i++) {
			IntAttached<ItemStack> entry = stacks.get(i);
			int remainingCount = entry.getFirst();
			boolean finalEntry = i == stacks.size() - 1;
			ItemStack requestedItem = entry.getSecond();

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

		previouslyUsedAddress = address;
		notifyUpdate();
	}

}
