package com.simibubi.create.api.contraption.storage.item.chest;

import com.mojang.serialization.Codec;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import com.simibubi.create.content.contraptions.Contraption;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * Mounted storage that handles opening a combined GUI for double chests.
 */
public class ChestMountedStorage extends SimpleMountedStorage {
	public static final Codec<SimpleMountedStorage> CODEC = SimpleMountedStorage.codec(ChestMountedStorage::new);

	protected ChestMountedStorage(SimpleMountedStorageType type, IItemHandlerModifiable handler) {
		super(type, handler);
	}

	public ChestMountedStorage(IItemHandlerModifiable handler) {
		this(AllMountedStorageTypes.CHEST.get(), handler);
	}

	@Override
	protected IItemHandlerModifiable getHandlerForMenu(StructureBlockInfo info, Contraption contraption) {
		BlockState state = info.state();
		ChestType type = state.getValue(ChestBlock.TYPE);
		if (type == ChestType.SINGLE)
			return this;

		Direction facing = state.getValue(ChestBlock.FACING);
		Direction connectedDirection = type == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
		BlockPos localOtherHalf = info.pos().relative(connectedDirection);
		MountedItemStorage otherHalf = contraption.getStorage().getMountedItems().storages.get(localOtherHalf);
		if (otherHalf == null)
			return this;

		if (type == ChestType.RIGHT) {
			return new CombinedInvWrapper(this, otherHalf);
		} else {
			return new CombinedInvWrapper(otherHalf, this);
		}
	}
}
