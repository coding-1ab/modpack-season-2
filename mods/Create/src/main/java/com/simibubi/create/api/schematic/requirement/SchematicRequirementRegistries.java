package com.simibubi.create.api.schematic.requirement;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.AttachedRegistry;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Registries for custom schematic requirements for blocks, block entities, and entities. These requirements determine
 * the items that are needed for placement into the world through schematics.
 * <p>
 * This is provided as an alternative to the following interfaces:
 * <ul>
 *     <li>{@link ISpecialBlockItemRequirement}</li>
 *     <li>{@link ISpecialBlockEntityItemRequirement}</li>
 *     <li>{@link ISpecialEntityItemRequirement}</li>
 * </ul>
 */
public class SchematicRequirementRegistries {
	public static final AttachedRegistry<Block, BlockRequirement> BLOCKS = AttachedRegistry.create();
	public static final AttachedRegistry<BlockEntityType<?>, BlockEntityRequirement> BLOCK_ENTITIES = AttachedRegistry.create();
	public static final AttachedRegistry<EntityType<?>, EntityRequirement> ENTITIES = AttachedRegistry.create();

	@FunctionalInterface
	public interface BlockRequirement {
		ItemRequirement getRequiredItems(Block block, BlockState state, @Nullable BlockEntity blockEntity);
	}

	@FunctionalInterface
	public interface BlockEntityRequirement {
		ItemRequirement getRequiredItems(BlockEntity blockEntity, BlockState state);
	}

	@FunctionalInterface
	public interface EntityRequirement {
		ItemRequirement getRequiredItems(Entity entity);
	}
}
