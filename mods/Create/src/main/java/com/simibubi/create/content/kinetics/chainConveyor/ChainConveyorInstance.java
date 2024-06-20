package com.simibubi.create.content.kinetics.chainConveyor;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

public class ChainConveyorInstance extends SingleRotatingInstance<ChainConveyorBlockEntity> {

	public ChainConveyorInstance(MaterialManager materialManager, ChainConveyorBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().getModel(AllPartialModels.CHAIN_CONVEYOR_SHAFT, blockEntity.getBlockState());
	}

	@Override
	protected Material<RotatingData> getRotatingMaterial() {
		return materialManager.defaultCutout()
			.material(AllMaterialSpecs.ROTATING);
	}

}
