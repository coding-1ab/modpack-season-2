package com.simibubi.create.content.logistics.packagerLink;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class LogisticallyLinkedBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<LogisticallyLinkedBehaviour> TYPE = new BehaviourType<>();

	public static final AtomicInteger LINK_ID_GENERATOR = new AtomicInteger();
	public int linkId; // Runtime context, not saved to disk

	public int redstonePower;
	public UUID freqId;

	//

	public static final Cache<UUID, Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>>> LINKS =
		CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.SECONDS)
			.build();

	public static Collection<LogisticallyLinkedBehaviour> getAllPresent(UUID freq, boolean sortByPriority) {
		Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>> cache = LINKS.getIfPresent(freq);
		if (cache == null)
			return Collections.emptyList();
		Stream<LogisticallyLinkedBehaviour> stream = new LinkedList<>(cache.asMap()
			.values()).stream()
				.map(WeakReference::get)
				.filter(LogisticallyLinkedBehaviour::isValidLink);

		if (sortByPriority)
			stream = stream.sorted((e1, e2) -> Integer.compare(e1.redstonePower, e2.redstonePower));

		return stream.toList();
	}

	public static void keepAlive(LogisticallyLinkedBehaviour behaviour) {
		if (behaviour.redstonePower == 15)
			return;
		try {
			Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>> cache = LINKS.get(behaviour.freqId,
				() -> CacheBuilder.newBuilder()
					.expireAfterAccess(1, TimeUnit.SECONDS)
					.build());

			if (cache == null)
				return;

			WeakReference<LogisticallyLinkedBehaviour> reference =
				cache.get(behaviour.linkId, () -> new WeakReference<>(behaviour));
			if (reference.get() != behaviour)
				cache.put(behaviour.linkId, new WeakReference<>(behaviour));

		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static void remove(LogisticallyLinkedBehaviour behaviour) {
		Cache<Integer, WeakReference<LogisticallyLinkedBehaviour>> cache = LINKS.getIfPresent(behaviour.freqId);
		if (cache != null)
			cache.invalidate(behaviour.linkId);
	}

	//

	public LogisticallyLinkedBehaviour(SmartBlockEntity be) {
		super(be);
		linkId = LINK_ID_GENERATOR.getAndIncrement();
		freqId = UUID.randomUUID();
	}

	@Override
	public void unload() {
		super.unload();
		remove(this);
	}

	@Override
	public void lazyTick() {
		keepAlive(this);
	}

	public void redstonePowerChanged(int power) {
		if (power == redstonePower)
			return;
		redstonePower = power;
		blockEntity.setChanged();

		if (power == 15)
			remove(this);
		else
			keepAlive(this);
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
		tag.putUUID("Freq", freqId);
		tag.putInt("Power", redstonePower);
	}

	@Override
	public void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		if (tag.hasUUID("Freq"))
			freqId = tag.getUUID("Freq");
		redstonePower = tag.getInt("Power");
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public Iterable<LogisticallyLinkedBehaviour> getAllConnectedAvailableLinks(boolean sortByPriority) {
		return getAllPresent(freqId, sortByPriority);
	}

}
