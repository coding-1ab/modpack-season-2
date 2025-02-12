package com.simibubi.create;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.AttachedRegistry;
import com.simibubi.create.content.contraptions.behaviour.DoorMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.LeverMovingInteraction;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.behaviour.TrapdoorMovingInteraction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AllInteractionBehaviours {
	public static final AttachedRegistry<Block, MovingInteractionBehaviour> REGISTRY = AttachedRegistry.create();

	@Nullable
	public static MovingInteractionBehaviour getBehaviour(BlockState state) {
		return REGISTRY.get(state.getBlock());
	}

	/**
	 * Creates a consumer that will register a behavior to a block. Useful for Registrate.
	 */
	public static <B extends Block> NonNullConsumer<? super B> interactionBehaviour(MovingInteractionBehaviour behaviour) {
		return b -> REGISTRY.register(b, behaviour);
	}

	static void registerDefaults() {
		REGISTRY.register(Blocks.LEVER, new LeverMovingInteraction());

		REGISTRY.registerProvider(AttachedRegistry.Provider.forBlockTag(BlockTags.WOODEN_DOORS, new DoorMovingInteraction()));
		REGISTRY.registerProvider(AttachedRegistry.Provider.forBlockTag(BlockTags.WOODEN_TRAPDOORS, new TrapdoorMovingInteraction()));
		REGISTRY.registerProvider(AttachedRegistry.Provider.forBlockTag(BlockTags.FENCE_GATES, new TrapdoorMovingInteraction()));
	}
}
