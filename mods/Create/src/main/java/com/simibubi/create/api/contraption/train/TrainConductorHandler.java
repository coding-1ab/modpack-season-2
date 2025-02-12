package com.simibubi.create.api.contraption.train;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlockBasedTrainConductorInteractionBehaviour;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


/**
 * All required methods to make your block a train conductor similar to the blaze burner
 */
@FunctionalInterface
public interface TrainConductorHandler {
	@ApiStatus.Internal
	List<TrainConductorHandler> CONDUCTOR_HANDLERS = new ArrayList<>();

	boolean isValidConductor(BlockState state);

	private static void registerHandler(TrainConductorHandler handler) {
		CONDUCTOR_HANDLERS.add(handler);
	}

	static void registerConductor(Block block, Predicate<BlockState> isValidConductor, UpdateScheduleCallback updateScheduleCallback) {
		BlockBasedTrainConductorInteractionBehaviour behavior = new BlockBasedTrainConductorInteractionBehaviour(isValidConductor, updateScheduleCallback);
		AllInteractionBehaviours.REGISTRY.register(block, behavior);
		registerHandler(isValidConductor::test);
	}

	@ApiStatus.Internal
	static void registerBlazeBurner(Block block) {
		registerConductor(block, blockState ->  AllBlocks.BLAZE_BURNER.has(blockState)
					&& blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL) != BlazeBurnerBlock.HeatLevel.NONE, UpdateScheduleCallback.EMPTY);
	}

	@FunctionalInterface
	interface UpdateScheduleCallback {
		UpdateScheduleCallback EMPTY = (hasSchedule, blockState, blockStateSetter) -> {};

		void update(boolean hasSchedule, BlockState currentBlockState, Consumer<BlockState> blockStateSetter);
	}
}
