package com.simibubi.create.impl.contraption.train;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.contraption.train.TrainConductorHandler;
import com.simibubi.create.api.contraption.train.TrainConductorHandler.UpdateScheduleCallback;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.minecraft.world.level.block.Block;

@ApiStatus.Internal
public class TrainConductorHandlerImpl {
	public static final List<TrainConductorHandler> CONDUCTOR_HANDLERS = new ArrayList<>();

	@ApiStatus.Internal
	public static void registerBlazeBurner(Block block) {
		TrainConductorHandler.registerConductor(block, blockState -> AllBlocks.BLAZE_BURNER.has(blockState)
			&& blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL) != BlazeBurnerBlock.HeatLevel.NONE, UpdateScheduleCallback.EMPTY);
	}
}
