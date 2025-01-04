package com.simibubi.create.api.contraption.storage.item;

import java.util.Objects;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import com.simibubi.create.content.contraptions.Contraption;

import com.simibubi.create.content.contraptions.MountedStorageInteraction;

import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

public abstract class MountedItemStorage implements IItemHandlerModifiable {
	public static final Codec<MountedItemStorage> CODEC = MountedItemStorageType.CODEC.dispatch(
		storage -> storage.type, type -> type.codec
	);

	public final MountedItemStorageType<? extends MountedItemStorage> type;

	protected MountedItemStorage(MountedItemStorageType<?> type) {
		this.type = Objects.requireNonNull(type);
	}

	/**
	 * Un-mount this storage back into the world. The expected storage type of the target
	 * block has already been checked to make sure it matches this storage's type.
	 */
	public abstract void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);

	/**
	 * @return true if train contraptions can search this storage for fuel.
	 * This is only called once on assembly.
	 */
	public boolean providesFuel() {
		return true;
	}

	/**
	 * Handle a player clicking on this mounted storage. This is always called on the server.
	 * The default implementation will try to open a generic GUI for standard inventories.
	 * For this to work, this storage must have 1-6 complete rows of 9 slots.
	 * @return true if the interaction was successful
	 */
	public boolean handleInteraction(Player player, Contraption contraption, StructureBlockInfo info) {
		IItemHandlerModifiable handler = this.getHandlerForMenu(info, contraption);
		int slots = handler.getSlots();
		if (slots == 0 || slots % 9 != 0)
			return false;

		int rows = slots / 9;
		if (rows > 6)
			return false;

		BlockPos localPos = info.pos();
		Vec3 globalPos = contraption.entity.toGlobalVector(Vec3.atCenterOf(localPos), 0);
		Predicate<Player> stillValid = p -> this.isMenuValid(p, contraption, globalPos);
		Component menuName = this.getMenuName(info, contraption);

		player.openMenu(MountedStorageInteraction.createMenuProvider(menuName, handler, rows, stillValid));
		this.playOpeningSound(player.level(), globalPos);
		return true;
	}

	/**
	 * Play the sound made by opening this storage's GUI.
	 * @see #handleInteraction(Player, Contraption, StructureBlockInfo)
	 */
	protected void playOpeningSound(Level level, Vec3 pos) {
		level.playSound(
			null, BlockPos.containing(pos),
			SoundEvents.BARREL_OPEN, SoundSource.BLOCKS,
			0.75f, 1f
		);
	}

	/**
	 * @return the title to be shown in the GUI when this storage is opened
	 * @see #handleInteraction(Player, Contraption, StructureBlockInfo)
	 */
	protected Component getMenuName(StructureBlockInfo info, Contraption contraption) {
		MutableComponent blockName = info.state().getBlock().getName();
		return CreateLang.translateDirect("contraptions.moving_container", blockName);
	}

	/**
	 * @param player the player who opened the menu
	 * @param pos the center of this storage in-world
	 * @return true if a GUI opened for this storage is still valid
	 */
	protected boolean isMenuValid(Player player, Contraption contraption, Vec3 pos) {
		return contraption.entity.isAlive() && player.distanceToSqr(pos) < (8 * 8);
	}

	/**
	 * Get the item handler that will be used by this storage's menu. This is useful for
	 * handling multi-blocks, such as double chests.
	 */
	protected IItemHandlerModifiable getHandlerForMenu(StructureBlockInfo info, Contraption contraption) {
		return this;
	}
}
