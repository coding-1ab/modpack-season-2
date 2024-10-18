package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.BlockFace;
import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class StockTickerBlockEntity extends StockCheckingBlockEntity implements IHaveHoveringInformation {

	// Player-interface Feature
	protected List<IntAttached<ItemStack>> lastClientsideStockSnapshot;
	protected InventorySummary lastClientsideStockSnapshotAsSummary;
	protected List<IntAttached<ItemStack>> newlyReceivedStockSnapshot;
	protected String previouslyUsedAddress;
	protected int activeLinks;
	protected int ticksSinceLastUpdate;

	// Auto-restock Feature
	protected InvManipulationBehaviour observedInventory;
	protected PackageOrder restockAmounts;
	protected String restockAddress;
	protected boolean powered;

	// Shop feature
	protected SmartInventory receivedPayments;
	protected LazyOptional<IItemHandler> capability;

	public StockTickerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		previouslyUsedAddress = "";
		restockAddress = "";
		restockAmounts = PackageOrder.empty();
		receivedPayments = new SmartInventory(27, this, 64, false);
		capability = LazyOptional.of(() -> receivedPayments);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours
			.add(observedInventory = new InvManipulationBehaviour(this, (w, p, s) -> new BlockFace(p, Direction.DOWN)));
	}

	public void refreshClientStockSnapshot() {
		ticksSinceLastUpdate = 0;
		AllPackets.getChannel()
			.sendToServer(new LogisticalStockRequestPacket(worldPosition));
	}

	public List<IntAttached<ItemStack>> getClientStockSnapshot() {
		return lastClientsideStockSnapshot;
	}

	public InventorySummary getLastClientsideStockSnapshotAsSummary() {
		return lastClientsideStockSnapshotAsSummary;
	}

	public int getTicksSinceLastUpdate() {
		return ticksSinceLastUpdate;
	}
	
	@Override
	public void broadcastPackageRequest(PackageOrder order, IItemHandler ignoredHandler, String address) {
		super.broadcastPackageRequest(order, ignoredHandler, address);
		previouslyUsedAddress = address;
		notifyUpdate();
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide()) {
			if (ticksSinceLastUpdate < 100)
				ticksSinceLastUpdate += 1;
			return;
		}
		int contributingLinks = getRecentSummary().contributingLinks;
		if (activeLinks != contributingLinks && !isRemoved()) {
			activeLinks = contributingLinks;
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
		tag.put("ReceivedPayments", receivedPayments.serializeNBT());

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
		receivedPayments.deserializeNBT(tag.getCompound("ReceivedPayments"));

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
		lastClientsideStockSnapshotAsSummary = new InventorySummary();
		stacks.forEach(lastClientsideStockSnapshotAsSummary::add);
		newlyReceivedStockSnapshot = null;
	}

	
	public boolean isKeeperPresent() {
		for (int yOffset : Iterate.zeroAndOne) {
			for (Direction side : Iterate.horizontalDirections) {
				BlockPos seatPos = worldPosition.below(yOffset)
					.relative(side);
				for (SeatEntity seatEntity : level.getEntitiesOfClass(SeatEntity.class, new AABB(seatPos)))
					if (seatEntity.isVehicle())
						return true;
			}
		}
		return false;
	}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (receivedPayments.isEmpty())
			return false;
		CreateLang.temporaryText("Contains payments:")
			.style(ChatFormatting.WHITE)
			.forGoggles(tooltip);

		InventorySummary summary = new InventorySummary();
		for (int i = 0; i < receivedPayments.getSlots(); i++)
			summary.add(receivedPayments.getStackInSlot(i));
		for (IntAttached<ItemStack> entry : summary.getStacksByCount())
			CreateLang.builder()
				.text(Components.translatable(entry.getSecond()
					.getDescriptionId())
					.getString() + " x" + entry.getFirst())
				.style(ChatFormatting.GREEN)
				.forGoggles(tooltip);

		CreateLang.temporaryText("Right-click to retrieve")
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);
		return true;
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (isItemHandlerCap(cap))
			return capability.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void destroy() {
		ItemHelper.dropContents(level, worldPosition, receivedPayments);
		super.destroy();
	}

	@Override
	public void invalidate() {
		capability.invalidate();
		super.invalidate();
	}

}
