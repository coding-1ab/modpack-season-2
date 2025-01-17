package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;

public class ShaftVisual<T extends KineticBlockEntity> extends SingleRotatingVisual<T> {

	public ShaftVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	@Override
	public RotatingInstance createRotatingInstance() {
		return instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
			.createInstance()
			.rotateToFace(rotationAxis());
	}
}
