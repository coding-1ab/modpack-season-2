package com.simibubi.create.api.contraption.train;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.content.processing.burner.BlockBasedTrainConductorInteractionBehaviour;
import com.simibubi.create.impl.contraption.train.TrainConductorHandlerImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


/**
 * All required methods to make your block a train conductor similar to the blaze burner
 */
@FunctionalInterface
public interface TrainConductorHandler {
	boolean isValidConductor(BlockState state);

	private static void registerHandler(TrainConductorHandler handler) {
		TrainConductorHandlerImpl.CONDUCTOR_HANDLERS.add(handler);
	}

	static void registerConductor(Block block, Predicate<BlockState> isValidConductor, UpdateScheduleCallback updateScheduleCallback) {
		BlockBasedTrainConductorInteractionBehaviour behavior = new BlockBasedTrainConductorInteractionBehaviour(isValidConductor, updateScheduleCallback);
		AllInteractionBehaviours.REGISTRY.register(block, behavior);
		registerHandler(isValidConductor::test);
	}

	interface UpdateScheduleCallback {
		UpdateScheduleCallback EMPTY = (hasSchedule, blockState, blockStateSetter) -> {};

		void update(boolean hasSchedule, BlockState currentBlockState, Consumer<BlockState> blockStateSetter);
	}
}
