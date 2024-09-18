package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;

public class ChainConveyorVisual extends SingleRotatingVisual<ChainConveyorBlockEntity> {

	public ChainConveyorVisual(VisualizationContext context, ChainConveyorBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	@Override
	protected Model model() {
		return Models.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT);
	}
}
