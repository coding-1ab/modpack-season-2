package com.simibubi.create.api.schematic.requirement;

import com.simibubi.create.content.schematics.requirement.ItemRequirement;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ISpecialBlockItemRequirement {
	ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity);
}
