package com.simibubi.create.api.contraption.transformable;

import com.simibubi.create.api.registry.AttachedRegistry;
import com.simibubi.create.content.contraptions.StructureTransform;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registry for custom transformations to apply to blocks after they've been moved by a contraption.
 * These interfaces are alternatives to the {@link ITransformableBlock} and {@link ITransformableBlockEntity} interfaces.
 */
public class MovedBlockTransformerRegistries {
	public static final AttachedRegistry<Block, BlockTransformer> BLOCK_TRANSFORMERS = AttachedRegistry.create();
	public static final AttachedRegistry<BlockEntityType<?>, BlockEntityTransformer> BLOCK_ENTITY_TRANSFORMERS = AttachedRegistry.create();

	@FunctionalInterface
	public interface BlockTransformer {
		BlockState transform(Block block, BlockState state, StructureTransform transform);
	}

	@FunctionalInterface
	public interface BlockEntityTransformer {
		void transform(BlockEntity be, StructureTransform transform);
	}
}
