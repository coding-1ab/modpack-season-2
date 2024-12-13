package com.simibubi.create.impl.behaviour;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;

import net.minecraft.resources.ResourceLocation;

// TODO - Make this use AttachedRegistry later
@ApiStatus.Internal
public class BlockSpoutingBehaviourImpl {
	private static final Map<ResourceLocation, BlockSpoutingBehaviour> BLOCK_SPOUTING_BEHAVIOURS = new ConcurrentHashMap<>();

	public static void addCustomSpoutInteraction(ResourceLocation resourceLocation, BlockSpoutingBehaviour spoutingBehaviour) {
		BLOCK_SPOUTING_BEHAVIOURS.put(resourceLocation, spoutingBehaviour);
	}

	public static void forEach(Consumer<? super BlockSpoutingBehaviour> accept) {
		BLOCK_SPOUTING_BEHAVIOURS.values().forEach(accept);
	}
}
