package com.simibubi.create.impl.schematic.nbt;

import com.simibubi.create.api.schematic.nbt.SchematicSafeNBTRegistry;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class SchematicSafeNBTRegistryImpl {
	private static final AttachedRegistry<BlockEntityType<? extends BlockEntity>, SchematicSafeNBTRegistry.ContextProvidingPartialSafeNBT> BLOCK_ENTITY_PARTIAL_SAFE_NBT = new AttachedRegistry<>(ForgeRegistries.BLOCK_ENTITY_TYPES);

	public static void register(BlockEntityType<? extends BlockEntity> blockEntityType, SchematicSafeNBTRegistry.ContextProvidingPartialSafeNBT safeNBT) {
		BLOCK_ENTITY_PARTIAL_SAFE_NBT.register(blockEntityType, safeNBT);
	}

	public static SchematicSafeNBTRegistry.ContextProvidingPartialSafeNBT getPartialSafeNBT(BlockEntityType<? extends BlockEntity> blockEntityType) {
		return BLOCK_ENTITY_PARTIAL_SAFE_NBT.get(blockEntityType);
	}
}
