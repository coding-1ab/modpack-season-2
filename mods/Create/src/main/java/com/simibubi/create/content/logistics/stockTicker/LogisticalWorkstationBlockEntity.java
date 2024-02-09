package com.simibubi.create.content.logistics.stockTicker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.createmod.catnip.utility.IntAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class LogisticalWorkstationBlockEntity extends SmartBlockEntity {

	private Map<Integer, IntAttached<WeakReference<PackagerLinkBlockEntity>>> connectedLinks = new HashMap<>();
	private InventorySummary summaryOfLinks;
	private int ticksSinceLastSummary;

	protected int activeLinks;

	public LogisticalWorkstationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
		ticksSinceLastSummary = 15;
		activeLinks = 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide())
			return;
		if (activeLinks != connectedLinks.size() && !isRemoved()) {
			activeLinks = connectedLinks.size();
			sendData();
		}
		if (ticksSinceLastSummary < 15)
			ticksSinceLastSummary++;
	}

	protected List<PackagerLinkBlockEntity> getAvailableLinks() {
		List<PackagerLinkBlockEntity> links = new ArrayList<>();
		connectedLinks.forEach(($, entry) -> {
			PackagerLinkBlockEntity blockEntity = entry.getSecond()
				.get();
			if (blockEntity != null && !blockEntity.isRemoved() && !blockEntity.isChunkUnloaded())
				links.add(blockEntity);
		});
		return links;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if (clientPacket)
			tag.putInt("ActiveLinks", activeLinks);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		if (clientPacket)
			activeLinks = tag.getInt("ActiveLinks");
	}

	public InventorySummary getRecentSummary() {
		if (summaryOfLinks == null || ticksSinceLastSummary >= 15)
			refreshInventorySummary();
		return summaryOfLinks;
	}

	protected void refreshInventorySummary() {
		ticksSinceLastSummary = 0;
		summaryOfLinks = new InventorySummary();
		connectedLinks.forEach(($, entry) -> {
			PackagerLinkBlockEntity link = entry.getSecond()
				.get();
			if (link != null && !link.isRemoved() && !link.isChunkUnloaded())
				summaryOfLinks.add(link.fetchSummaryFromPackager());
		});
	}

	@Override
	public void lazyTick() {
		if (level.isClientSide())
			return;
		for (Iterator<Integer> iterator = connectedLinks.keySet()
			.iterator(); iterator.hasNext();) {
			Integer id = iterator.next();
			IntAttached<WeakReference<PackagerLinkBlockEntity>> entry = connectedLinks.get(id);
			entry.decrement();
			if (entry.isOrBelowZero()) {
				iterator.remove();
				continue;
			}
			PackagerLinkBlockEntity link = entry.getSecond()
				.get();
			if (link == null || link.isRemoved() || link.isChunkUnloaded()) {
				iterator.remove();
				continue;
			}
		}
	}

	public void keepConnected(PackagerLinkBlockEntity link) {
		connectedLinks.computeIfAbsent(link.linkId, $ -> IntAttached.withZero(new WeakReference<>(link)))
			.setFirst(3);
	}

	public void invalidateLink(PackagerLinkBlockEntity link) {
		connectedLinks.remove(link.linkId);
	}

}
