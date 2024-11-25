package com.simibubi.create.content.kinetics.saw;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.render.VirtualRenderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawVisual extends SingleRotatingVisual<SawBlockEntity> {

	public SawVisual(VisualizationContext context, SawBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	@Override
	protected Model model() {
		return shaftModel(level, pos, blockState);
	}

	public static Model shaftModel(LevelAccessor level, BlockPos pos, BlockState state) {
		if (state.getValue(BlockStateProperties.FACING)
			.getAxis()
			.isHorizontal()) {
			BlockState referenceState = state.rotate(level, pos, Rotation.CLOCKWISE_180);
			Direction facing = referenceState.getValue(BlockStateProperties.FACING);
			return Models.partial(AllPartialModels.SHAFT_HALF, facing);
		} else {
			return VirtualRenderHelper.blockModel(KineticBlockEntityVisual.shaft(state));
		}
	}
}
