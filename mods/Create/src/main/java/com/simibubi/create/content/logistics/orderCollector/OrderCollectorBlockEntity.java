package com.simibubi.create.content.logistics.orderCollector;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.REMOVE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class OrderCollectorBlockEntity extends KineticBlockEntity {

	protected Map<Integer, List<ItemStack>> collectedPackages = new HashMap<>();
	protected List<ItemStack> exportingPackages = new ArrayList<>();

	protected ItemStack visuallyCollectingPackage = ItemStack.EMPTY;
	protected int collectingAnimationTicks = 0;
	protected ItemStack visuallyDeployingPackage = ItemStack.EMPTY;
	protected int deployingAnimationTicks = 0;

	private int exportCooldown = 0;

	public OrderCollectorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().expandTowards(0, -2, 0);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
			.whileItemHeld(this::whenItemHeld));
	}

	protected ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (handler.blockEntity.isVirtual())
			return PASS;
		if (getSpeed() == 0)
			return PASS;
		if (!(transported.stack.getItem() instanceof PackageItem))
			return PASS;

		int orderId = PackageItem.getOrderId(transported.stack);
		if (orderId == -1)
			return PASS;
		return HOLD;
	}

	protected ProcessingResult whenItemHeld(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (!visuallyCollectingPackage.isEmpty())
			return HOLD;
		if (!visuallyDeployingPackage.isEmpty())
			return HOLD;

		ItemStack box = transported.stack.copy();
		addPackageFragment(box);

		visuallyCollectingPackage = box;
		collectingAnimationTicks = 0;

		handler.handleProcessingOnItem(transported, TransportedResult.removeItem());

		return REMOVE;
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);

		boolean wasCollecting = !visuallyCollectingPackage.isEmpty();
		visuallyCollectingPackage = ItemStack.of(compound.getCompound("CollectingItem"));
		if (!wasCollecting)
			collectingAnimationTicks = 0;

		boolean wasDeploying = !visuallyDeployingPackage.isEmpty();
		visuallyDeployingPackage = ItemStack.of(compound.getCompound("DeployingItem"));
		if (!wasDeploying)
			deployingAnimationTicks = 0;

		if (clientPacket)
			return;

		collectedPackages.clear();
		NBTHelper.iterateCompoundList(compound.getList("CollectedOrders", Tag.TAG_COMPOUND), tag -> collectedPackages
			.put(tag.getInt("Id"), NBTHelper.readItemList(tag.getList("Boxes", Tag.TAG_COMPOUND))));

		exportingPackages = NBTHelper.readItemList(compound.getList("ExportingItems", Tag.TAG_COMPOUND));
		exportCooldown = compound.getInt("ExportCooldown");
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		compound.put("CollectingItem", visuallyCollectingPackage.serializeNBT());
		compound.put("DeployingItem", visuallyDeployingPackage.serializeNBT());

		if (clientPacket)
			return;

		ListTag collectedNbt = new ListTag();
		for (Entry<Integer, List<ItemStack>> entry : collectedPackages.entrySet()) {
			CompoundTag tag = new CompoundTag();
			tag.putInt("Id", entry.getKey());
			tag.put("Boxes", NBTHelper.writeItemList(entry.getValue()));
			collectedNbt.add(tag);
		}
		compound.put("CollectedOrders", collectedNbt);

		compound.put("ExportingItems", NBTHelper.writeItemList(exportingPackages));
		compound.putInt("ExportCooldown", exportCooldown);
	}

	@Override
	public void destroy() {
		super.destroy();
		collectedPackages.values()
			.forEach(exportingPackages::addAll);
		exportingPackages.add(visuallyDeployingPackage);
		for (ItemStack box : exportingPackages)
			Block.popResource(level, worldPosition, box);
	}

	@Override
	public void tick() {
		super.tick();

		if (!visuallyCollectingPackage.isEmpty()) {
			collectingAnimationTicks++;
			if (collectingAnimationTicks == 20)
				visuallyCollectingPackage = ItemStack.EMPTY;
			exportCooldown = 1;
		}

		if (!visuallyDeployingPackage.isEmpty()) {
			deployingAnimationTicks++;
			if (deployingAnimationTicks == 10) {
				if (!level.isClientSide()) {
					DirectBeltInputBehaviour directBeltInputBehaviour =
						BlockEntityBehaviour.get(level, worldPosition.below(2), DirectBeltInputBehaviour.TYPE);
					if (directBeltInputBehaviour != null) {
						ItemStack remainder =
							directBeltInputBehaviour.handleInsertion(visuallyDeployingPackage, Direction.DOWN, false);
						if (!remainder.isEmpty())
							Block.popResource(level, worldPosition.below(), remainder);
					}
				}
				visuallyDeployingPackage = ItemStack.EMPTY;
			}
			exportCooldown = 1;
		}

		if (level.isClientSide())
			return;
		if (getSpeed() == 0)
			return;
		if (exportCooldown > 0) {
			exportCooldown--;
			return;
		}
		if (exportingPackages.isEmpty())
			return;

		exportCooldown = 10;

		TransportedItemStackHandlerBehaviour transporter =
			BlockEntityBehaviour.get(level, worldPosition.below(2), TransportedItemStackHandlerBehaviour.TYPE);
		MutableBoolean hasSpace = new MutableBoolean(true);
		if (transporter != null)
			transporter.handleCenteredProcessingOnAllItems(0.51f, $ -> {
				hasSpace.setFalse();
				return TransportedResult.doNothing();
			});

		visuallyDeployingPackage = exportingPackages.get(0);
		deployingAnimationTicks = 0;
		exportingPackages.remove(0);
		notifyUpdate();
	}

	private void addPackageFragment(ItemStack box) {
		int collectedOrderId = PackageItem.getOrderId(box);
		List<ItemStack> collectedOrder = collectedPackages.computeIfAbsent(collectedOrderId, $ -> Lists.newArrayList());
		collectedOrder.add(box);

		if (!isOrderComplete(collectedOrderId)) {
			notifyUpdate();
			return;
		}

		repack(collectedOrderId);
		exportCooldown = 1;
	}

	private void repack(int orderId) {
		String address = "";
		PackageOrder order = null;
		List<IntAttached<ItemStack>> allItems = new ArrayList<>();

		for (ItemStack box : collectedPackages.get(orderId)) {
			address = PackageItem.getAddress(box);
			if (box.hasTag() && box.getTag()
				.getCompound("Fragment")
				.contains("OrderContext"))
				order = PackageOrder.read(box.getTag()
					.getCompound("Fragment")
					.getCompound("OrderContext"));
			ItemStackHandler contents = PackageItem.getContents(box);
			Slots: for (int slot = 0; slot < contents.getSlots(); slot++) {
				ItemStack stackInSlot = contents.getStackInSlot(slot);
				for (IntAttached<ItemStack> existing : allItems) {
					if (!ItemHandlerHelper.canItemStacksStack(stackInSlot, existing.getValue()))
						continue;
					existing.setFirst(existing.getFirst() + stackInSlot.getCount());
					continue Slots;
				}
				allItems.add(IntAttached.with(stackInSlot.getCount(), stackInSlot));
			}
		}

		List<IntAttached<ItemStack>> orderedStacks = order == null ? Collections.emptyList() : order.stacks();
		List<ItemStack> outputSlots = new ArrayList<>();

		Repack: while (true) {
			allItems.removeIf(e -> e.getFirst() == 0);
			if (allItems.isEmpty())
				break;

			IntAttached<ItemStack> targetedEntry = null;
			if (!orderedStacks.isEmpty())
				targetedEntry = orderedStacks.remove(0);

			ItemSearch: for (IntAttached<ItemStack> entry : allItems) {
				int targetAmount = entry.getFirst();
				if (targetAmount == 0)
					continue;
				if (targetedEntry != null) {
					targetAmount = targetedEntry.getFirst();
					if (!ItemHandlerHelper.canItemStacksStack(entry.getSecond(), targetedEntry.getSecond()))
						continue;
				}

				while (targetAmount > 0) {
					int removedAmount = Math.min(Math.min(targetAmount, entry.getSecond()
						.getMaxStackSize()), entry.getFirst());
					if (removedAmount == 0)
						continue ItemSearch;

					ItemStack output = ItemHandlerHelper.copyStackWithSize(entry.getSecond(), removedAmount);
					targetAmount -= removedAmount;
					targetedEntry.setFirst(targetAmount);
					entry.setFirst(entry.getFirst() - removedAmount);
					outputSlots.add(output);
				}

				continue Repack;
			}
		}

		int currentSlot = 0;
		ItemStackHandler target = new ItemStackHandler(PackageItem.SLOTS);

		for (ItemStack item : outputSlots) {
			target.setStackInSlot(currentSlot++, item);
			if (currentSlot < PackageItem.SLOTS)
				continue;
			exportingPackages.add(PackageItem.containing(target));
			target = new ItemStackHandler(PackageItem.SLOTS);
			currentSlot = 0;
		}

		exportingPackages.add(PackageItem.containing(target));

		for (ItemStack box : exportingPackages)
			PackageItem.addAddress(box, address);

		collectedPackages.remove(orderId);
		notifyUpdate();
	}

	private boolean isOrderComplete(int orderId) {
		boolean finalLinkReached = false;
		Links: for (int linkCounter = 0; linkCounter < 1000; linkCounter++) {
			if (finalLinkReached)
				break;
			Packages: for (int packageCounter = 0; packageCounter < 1000; packageCounter++) {
				for (ItemStack box : collectedPackages.get(orderId)) {
					CompoundTag tag = box.getOrCreateTag()
						.getCompound("Fragment");
					if (linkCounter != tag.getInt("LinkIndex"))
						continue;
					if (packageCounter != tag.getInt("Index"))
						continue;
					finalLinkReached = tag.getBoolean("IsFinalLink");
					if (tag.getBoolean("IsFinal"))
						continue Links;
					continue Packages;
				}
				return false;
			}
		}
		return true;
	}

}
