package com.simibubi.create.impl.contraption.transformable;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.api.contraption.transformable.ContraptionTransformableRegistry.TransformableBlock;
import com.simibubi.create.api.contraption.transformable.ContraptionTransformableRegistry.TransformableBlockEntity;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;

@ApiStatus.Internal
public class ContraptionTransformableRegistryImpl {
	private static final AttachedRegistry<Block, TransformableBlock> TRANSFORMABLE_BLOCKS = new AttachedRegistry<>(ForgeRegistries.BLOCKS);
	private static final AttachedRegistry<BlockEntityType<?>, TransformableBlockEntity> TRANSFORMABLE_BLOCK_ENTITIES = new AttachedRegistry<>(ForgeRegistries.BLOCK_ENTITY_TYPES);

	public static void registerForBlock(Block block, TransformableBlock transformableBlock) {
		TRANSFORMABLE_BLOCKS.register(block, transformableBlock);
	}

	public static void registerForBlockEntity(BlockEntityType<?> blockEntityType, TransformableBlockEntity transformableBlockEntity) {
		TRANSFORMABLE_BLOCK_ENTITIES.register(blockEntityType, transformableBlockEntity);
	}

	public static TransformableBlock get(Block block) {
		return TRANSFORMABLE_BLOCKS.get(block);
	}

	public static TransformableBlockEntity get(BlockEntityType<?> blockEntityType) {
		return TRANSFORMABLE_BLOCK_ENTITIES.get(blockEntityType);
	}
}
