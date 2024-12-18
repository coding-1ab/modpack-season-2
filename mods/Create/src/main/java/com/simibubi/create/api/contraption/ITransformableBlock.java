package com.simibubi.create.api.contraption;

import com.simibubi.create.content.contraptions.StructureTransform;

import net.minecraft.world.level.block.state.BlockState;

public interface ITransformableBlock {
	BlockState transform(BlockState state, StructureTransform transform);
}
