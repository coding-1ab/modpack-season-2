package com.simibubi.create.content.logistics.packagerLink;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.IntAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

public class LogisticallyLinkedBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<LogisticallyLinkedBehaviour> TYPE = new BehaviourType<>();
	public static final AtomicInteger LINK_ID_GENERATOR = new AtomicInteger();

	public int redstonePower;

	// To
	public int linkId;
	public BlockPos targetOffset;
	public ResourceKey<Level> targetDim;
	public WeakReference<LogisticallyLinkedBehaviour> target;

	// From
	private Map<Integer, IntAttached<WeakReference<LogisticallyLinkedBehaviour>>> connectedLinks = new HashMap<>();

	public LogisticallyLinkedBehaviour(SmartBlockEntity be) {
		super(be);
		targetOffset = BlockPos.ZERO;
		linkId = LINK_ID_GENERATOR.getAndIncrement();
	}

	@Override
	public void unload() {
		super.unload();
		if (!getWorld().isClientSide())
			invalidateSelf();
	}

	public void invalidateSelf() {
		if (getTarget() instanceof LogisticallyLinkedBehaviour target)
			target.invalidateLink(this);
	}

	@Nullable
	public LogisticallyLinkedBehaviour getTarget() {
		if (targetOffset.equals(BlockPos.ZERO))
			return null;

		if (target != null) {
			LogisticallyLinkedBehaviour link = target.get();
			if (isValidLink(link))
				return link;
			target = null;
		}

		BlockPos targetPos = getPos().offset(targetOffset);
		Level world = getWorld();

		if (world instanceof ServerLevel sl)
			world = sl.getServer()
				.getLevel(targetDim);
		if (world == null)
			return null;

		if (world.isLoaded(targetPos) && get(world, targetPos, TYPE) instanceof LogisticallyLinkedBehaviour llb) {
			target = new WeakReference<LogisticallyLinkedBehaviour>(llb);
			return llb;
		}

		return null;
	}

	@Override
	public void lazyTick() {
		if (getWorld().isClientSide())
			return;

		tickTargetConnection();

		for (Iterator<Integer> iterator = connectedLinks.keySet()
			.iterator(); iterator.hasNext();) {
			Integer id = iterator.next();
			IntAttached<WeakReference<LogisticallyLinkedBehaviour>> entry = connectedLinks.get(id);
			entry.decrement();
			if (entry.isOrBelowZero()) {
				iterator.remove();
				continue;
			}
			LogisticallyLinkedBehaviour link = entry.getSecond()
				.get();
			if (!isValidLink(link)) {
				iterator.remove();
				continue;
			}
		}
	}

	public void tickTargetConnection() {
		if (getTarget() instanceof LogisticallyLinkedBehaviour target)
			target.keepConnected(this);
	}

	public void redstonePowerChanged(int power) {
		if (power == redstonePower)
			return;
		redstonePower = power;
		blockEntity.setChanged();

		if (power == 15)
			invalidateSelf();
		else
			tickTargetConnection();
	}

	public List<LogisticallyLinkedBehaviour> getAllConnectedAvailableLinks(boolean sortByPriority) {
		Map<LogisticallyLinkedBehaviour, Integer> links = new IdentityHashMap<>();
		appendAvailableLinksRecursive(links, 0);

		if (!sortByPriority)
			return new ArrayList<>(links.keySet());

		return new ArrayList<>(links.entrySet()).stream()
			.sorted((e1, e2) -> e1.getValue()
				.compareTo(e2.getValue()))
			.map(Entry::getKey)
			.toList();
	}

	private void appendAvailableLinksRecursive(Map<LogisticallyLinkedBehaviour, Integer> links, int relativePower) {
		if (redstonePower == 15)
			return;

		int combinedPower = relativePower + redstonePower;
		links.put(this, combinedPower);

		if (getTarget() instanceof LogisticallyLinkedBehaviour target && !links.containsKey(target))
			target.appendAvailableLinksRecursive(links, combinedPower);

		connectedLinks.forEach(($, entry) -> {
			LogisticallyLinkedBehaviour link = entry.getSecond()
				.get();
			if (isValidLink(link) && !links.containsKey(link))
				link.appendAvailableLinksRecursive(links, combinedPower);
		});
	}

	public int processRequest(ItemStack stack, int amount, String address, int linkIndex, MutableBoolean finalLink,
		int orderId, @Nullable PackageOrder orderContext, @Nullable IItemHandler ignoredHandler) {
		if (blockEntity instanceof PackagerLinkBlockEntity plbe)
			return plbe.processRequest(stack, amount, address, linkIndex, finalLink, orderId, orderContext,
				ignoredHandler);
		return 0;
	}

	public InventorySummary getSummary() {
		if (blockEntity instanceof PackagerLinkBlockEntity plbe)
			return plbe.fetchSummaryFromPackager();
		return InventorySummary.EMPTY;
	}

	//

	public static boolean isValidLink(LogisticallyLinkedBehaviour link) {
		return link != null && !link.blockEntity.isRemoved() && !link.blockEntity.isChunkUnloaded();
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.put("TargetOffset", NbtUtils.writeBlockPos(targetOffset));
		if (targetDim != null)
			tag.putString("TargetDimension", targetDim.location()
				.toString());
		tag.putInt("Power", redstonePower);
	}

	@Override
	public void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		targetOffset = NbtUtils.readBlockPos(tag.getCompound("TargetOffset"));
		redstonePower = tag.getInt("Power");

		if (!tag.contains("TargetDimension")) {
			targetDim = Level.OVERWORLD;
			return;
		}

		targetDim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("TargetDimension")));
	}

	public void keepConnected(LogisticallyLinkedBehaviour link) {
		connectedLinks.computeIfAbsent(link.linkId, $ -> IntAttached.withZero(new WeakReference<>(link)))
			.setFirst(3);
	}

	public void invalidateLink(LogisticallyLinkedBehaviour link) {
		connectedLinks.remove(link.linkId);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
