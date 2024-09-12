package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StockCheckingBlockEntity extends SmartBlockEntity {

	public LogisticallyLinkedBehaviour behaviour;

	protected InventorySummary summaryOfLinks;
	protected int activeLinksLastSummary;
	protected int ticksSinceLastSummary;

	public StockCheckingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
		ticksSinceLastSummary = 15;
		activeLinksLastSummary = 0;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide())
			return;
		if (ticksSinceLastSummary < 15)
			ticksSinceLastSummary++;
	}

	public InventorySummary getRecentSummary() {
		if (summaryOfLinks == null || ticksSinceLastSummary >= 15)
			refreshInventorySummary();
		return summaryOfLinks;
	}

	protected void refreshInventorySummary() {
		ticksSinceLastSummary = 0;
		activeLinksLastSummary = 0;
		summaryOfLinks = new InventorySummary();
		behaviour.getAllConnectedAvailableLinks(false)
			.forEach(link -> {
				InventorySummary summary = link.getSummary();
				if (summary != InventorySummary.EMPTY)
					activeLinksLastSummary++;
				summaryOfLinks.add(summary);
			});
	}

}
