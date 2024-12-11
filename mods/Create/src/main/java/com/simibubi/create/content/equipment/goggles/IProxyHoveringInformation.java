package com.simibubi.create.content.equipment.goggles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implement this interface on the {@link BlockEntity} that wants to add info to the goggle overlay
 */
public interface IProxyHoveringInformation {
	BlockPos getInformationSource(Level level, BlockPos pos, BlockState state);
}
