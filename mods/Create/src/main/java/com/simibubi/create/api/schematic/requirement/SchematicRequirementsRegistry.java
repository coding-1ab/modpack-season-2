package com.simibubi.create.api.schematic.requirement;

import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import com.simibubi.create.impl.schematic.requirement.SchematicRequirementsRegistryImpl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Registry for schematic requirements for blocks, block entities, and entities.
 */
public class SchematicRequirementsRegistry {
	/**
	 * Register a new special requirement for a specified block
	 *
	 * @param block The block you want to register a {@link BlockRequirement} for
	 * @param requirement The requirement you would like to add to this block,
	 *                       the {@link BlockRequirement#getRequiredItems(BlockState, BlockEntity)}
	 *                       method will be called on the {@link BlockRequirement} you have provided,
	 *                       and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlock(Block block, BlockRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlock(block, requirement);
	}

	/**
	 * Register a new special requirement for a specified block
	 *
	 * @param block The id of the block you want to register a {@link BlockRequirement} for
	 * @param requirement The requirement you would like to add to this block,
	 *                       the {@link BlockRequirement#getRequiredItems(BlockState, BlockEntity)}
	 *                       method will be called on the {@link BlockRequirement} you have provided,
	 *                       and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlock(ResourceLocation block, BlockRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlock(block, requirement);
	}

	/**
	 * Register a new special requirement for a specified block entity type
	 *
	 * @param blockEntityType The blockEntityType you want to register a {@link BlockEntityRequirement} for
	 * @param requirement The requirement you would like to add to this block entity type,
	 *                       the {@link BlockEntityRequirement#getRequiredItems(BlockEntity, BlockState)}
	 *                       method will be called on the {@link BlockEntityRequirement} you have provided,
	 *                       and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlockEntity(BlockEntityType<BlockEntity> blockEntityType, BlockEntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlockEntity(blockEntityType, requirement);
	}

	/**
	 * Register a new special requirement for a specified block entity type
	 *
	 * @param blockEntityType The id of the blockEntityType you want to register a {@link BlockEntityRequirement} for
	 * @param requirement The requirement you would like to add to this block entity type,
	 *                       the {@link BlockEntityRequirement#getRequiredItems(BlockEntity, BlockState)}
	 *                       method will be called on the {@link BlockEntityRequirement} you have provided,
	 *                       and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForBlockEntity(ResourceLocation blockEntityType, BlockEntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForBlockEntity(blockEntityType, requirement);
	}

	/**
	 * Register a new special requirement for a specified entity type
	 *
	 * @param entityType The entityType you want to register a {@link EntityRequirement} for
	 * @param requirement The requirement you would like to add to this entity type,
	 *                       the {@link EntityRequirement#getRequiredItems(Entity)}
	 *                       method will be called on the {@link EntityRequirement} you have provided,
	 *                       and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForEntity(EntityType<Entity> entityType, EntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForEntity(entityType, requirement);
	}

	/**
	 * Register a new special requirement for a specified entity type
	 *
	 * @param entityType The id of the entityType you want to register a {@link EntityRequirement} for
	 * @param requirement The requirement you would like to add to this entity type,
	 *                       the {@link EntityRequirement#getRequiredItems(Entity)}
	 *                       method will be called on the {@link EntityRequirement} you have provided,
	 *                       and you will be able to insert requirements based off the context that is given
	 */
	public static void registerForEntity(ResourceLocation entityType, EntityRequirement requirement) {
		SchematicRequirementsRegistryImpl.registerForEntity(entityType, requirement);
	}

	// --- Interfaces that provide the context that would be accessible if you implemented the ISpecial* interfaces ---

	@FunctionalInterface
	public interface BlockRequirement {
		ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity);
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

