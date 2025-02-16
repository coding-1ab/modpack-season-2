package com.simibubi.create;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.behaviour.BellMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.CampfireMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.dispenser.DispenserMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.dispenser.DropperMovementBehaviour;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AllMovementBehaviours {
	public static final SimpleRegistry<Block, MovementBehaviour> REGISTRY = SimpleRegistry.create();

	@Nullable
	public static MovementBehaviour getBehaviour(BlockState state) {
		return REGISTRY.get(state.getBlock());
	}

	/**
	 * Creates a consumer that will register a behavior to a block. Useful for Registrate.
	 */
	public static <B extends Block> NonNullConsumer<? super B> movementBehaviour(MovementBehaviour behaviour) {
		return b -> REGISTRY.register(b, behaviour);
	}

	static void registerDefaults() {
		REGISTRY.register(Blocks.BELL, new BellMovementBehaviour());
		REGISTRY.register(Blocks.CAMPFIRE, new CampfireMovementBehaviour());
		REGISTRY.register(Blocks.SOUL_CAMPFIRE, new CampfireMovementBehaviour());

		DispenserMovementBehaviour.gatherMovedDispenseItemBehaviours();
		REGISTRY.register(Blocks.DISPENSER, new DispenserMovementBehaviour());
		REGISTRY.register(Blocks.DROPPER, new DropperMovementBehaviour());
	}
}
