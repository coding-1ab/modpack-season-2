package com.simibubi.create.content.fluids.tank;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BoilerHeaters {
	public static final int PASSIVE_HEAT = 0;
	public static final int NO_HEAT = -1;

	public static final SimpleRegistry<Block, Heater> REGISTRY = SimpleRegistry.create();

	public static final Heater PASSIVE_HEATER = (level, pos, state) -> BlockHelper.isNotUnheated(state) ? PASSIVE_HEAT : NO_HEAT;

	/**
	 * Gets the heat at the given location. If a heater is present, queries it for heat. If not, returns {@link #NO_HEAT}.
	 * @see Heater#getActiveHeat(Level, BlockPos, BlockState)
	 */
	public static float getActiveHeat(Level level, BlockPos pos, BlockState state) {
		Heater heater = REGISTRY.get(state.getBlock());
		return heater != null ? heater.getActiveHeat(level, pos, state) : NO_HEAT;
	}

	public static void registerDefaults() {
		REGISTRY.register(AllBlocks.BLAZE_BURNER.get(), (level, pos, state) -> {
			HeatLevel value = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
			if (value == HeatLevel.NONE) {
				return NO_HEAT;
			}
			if (value == HeatLevel.SEETHING) {
				return 2;
			}
			if (value.isAtLeast(HeatLevel.FADING)) {
				return 1;
			}
			return PASSIVE_HEAT;
		});

		REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(AllBlockTags.PASSIVE_BOILER_HEATERS.tag, PASSIVE_HEATER));
	}

	@FunctionalInterface
	public interface Heater {
		/**
		 * @return the amount of heat to provide.
		 * @see #NO_HEAT
		 * @see #PASSIVE_HEAT
		 */
		float getActiveHeat(Level level, BlockPos pos, BlockState state);
	}
}
