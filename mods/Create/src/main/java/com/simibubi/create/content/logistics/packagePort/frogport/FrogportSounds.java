package com.simibubi.create.content.logistics.packagePort.frogport;

import java.util.List;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllSoundEvents.SoundEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FrogportSounds {

	private static final List<SoundEntry> CATCH_SOUNDS = List.of(AllSoundEvents.FROGPORT_CATCH_1,
		AllSoundEvents.FROGPORT_CATCH_2, AllSoundEvents.FROGPORT_CATCH_3, AllSoundEvents.FROGPORT_CATCH_4);

	public void open(Level level, BlockPos pos) {
		AllSoundEvents.FROGPORT_OPEN.playAt(level, Vec3.atCenterOf(pos), 1, 1, false);
	}

	public void close(Level level, BlockPos pos) {
		if (!isPlayerNear(pos))
			return;
		AllSoundEvents.FROGPORT_CLOSE.playAt(level, Vec3.atCenterOf(pos), 0.5f, 1.25f + level.random.nextFloat() * 0.25f,
			true);
	}

	public void catchPackage(Level level, BlockPos pos) {
		if (!isPlayerNear(pos))
			return;
		CATCH_SOUNDS.get(level.random.nextInt(CATCH_SOUNDS.size()))
			.playAt(level, Vec3.atCenterOf(pos), 1, 1, false);
	}

	public void depositPackage(Level level, BlockPos pos) {
		if (!isPlayerNear(pos))
			return;
		AllSoundEvents.FROGPORT_DEPOSIT.playAt(level, Vec3.atCenterOf(pos), 1, 1, false);
	}

	private boolean isPlayerNear(BlockPos pos) {
		return pos.closerThan(Minecraft.getInstance().player.blockPosition(), 20);
	}

}
