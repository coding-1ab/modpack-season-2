package com.simibubi.create.content.contraptions;

import java.util.Collection;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.api.registry.AttachedRegistry;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import net.minecraftforge.common.extensions.IForgeBlock;

public enum ContraptionMovementSetting {
	MOVABLE, NO_PICKUP, UNMOVABLE;

	public static final AttachedRegistry<Block, Supplier<ContraptionMovementSetting>> REGISTRY = AttachedRegistry.create();

	@Nullable
	public static ContraptionMovementSetting get(Block block) {
		if (block instanceof IMovementSettingProvider provider)
			return provider.getContraptionMovementSetting();
		Supplier<ContraptionMovementSetting> supplier = REGISTRY.get(block);
		return supplier == null ? null : supplier.get();
	}

	public static boolean allAre(Collection<StructureTemplate.StructureBlockInfo> blocks, ContraptionMovementSetting are) {
		return blocks.stream().anyMatch(b -> get(b.state().getBlock()) == are);
	}

	public static boolean isNoPickup(Collection<StructureTemplate.StructureBlockInfo> blocks) {
		return allAre(blocks, ContraptionMovementSetting.NO_PICKUP);
	}

	public static void registerDefaults() {
		REGISTRY.register(Blocks.SPAWNER, () -> AllConfigs.server().kinetics.spawnerMovement.get());
		REGISTRY.register(Blocks.BUDDING_AMETHYST, () -> AllConfigs.server().kinetics.amethystMovement.get());
		REGISTRY.register(Blocks.OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		REGISTRY.register(Blocks.CRYING_OBSIDIAN, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		REGISTRY.register(Blocks.RESPAWN_ANCHOR, () -> AllConfigs.server().kinetics.obsidianMovement.get());
		REGISTRY.register(Blocks.REINFORCED_DEEPSLATE, () -> AllConfigs.server().kinetics.reinforcedDeepslateMovement.get());
	}

	public interface IMovementSettingProvider extends IForgeBlock {
		ContraptionMovementSetting getContraptionMovementSetting();
	}
}
