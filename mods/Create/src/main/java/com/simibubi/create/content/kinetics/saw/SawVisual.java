package com.simibubi.create.content.kinetics.saw;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawVisual extends SingleRotatingVisual<SawBlockEntity> {

	public SawVisual(VisualizationContext context, SawBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	@Override
	public RotatingInstance createRotatingInstance() {
		return shaft(instancerProvider(), level, pos, blockState);
	}

	public static RotatingInstance shaft(InstancerProvider instancerProvider, LevelAccessor level, BlockPos pos, BlockState state) {
		var facing = state.getValue(BlockStateProperties.FACING);
		var axis = facing
			.getAxis();
		if (axis.isHorizontal()) {
			Direction align = facing.getOpposite();
			return instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
				.createInstance()
				.rotateTo(0, 0, 1, align.getStepX(), align.getStepY(), align.getStepZ());
		} else {
			return instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
				.createInstance()
				.rotateToFace(state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? Axis.X : Axis.Z);
		}
	}
}
