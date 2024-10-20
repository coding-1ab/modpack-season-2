package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record FactoryPanelConnection(FactoryPanelPosition from, int amount, List<Direction> path,
	MutableBoolean successTracker) {

	public FactoryPanelConnection(FactoryPanelPosition from, int amount) {
		this(from, amount, new ArrayList<>(), new MutableBoolean(true));
	}

	public static FactoryPanelConnection read(CompoundTag nbt) {
		return new FactoryPanelConnection(FactoryPanelPosition.read(nbt), nbt.getInt("Amount"));
	}

	public CompoundTag write() {
		CompoundTag nbt = from.write();
		nbt.putInt("Amount", amount);
		return nbt;
	}

	public List<Direction> getPath(BlockState state, FactoryPanelPosition to) {
		if (!path.isEmpty())
			return path;

		path.clear();

		float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
		float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);

		int slotDiffx = to.slot().xOffset - from.slot().xOffset;
		int slotDiffY = to.slot().yOffset - from.slot().yOffset;
		Vec3 diff = Vec3.atLowerCornerOf(to.pos()
			.subtract(from.pos()));

		diff = VecHelper.rotate(diff, -yRot, Axis.Y);
		diff = VecHelper.rotate(diff, -xRot - 90, Axis.X);
		diff = VecHelper.rotate(diff, -180, Axis.Y);
		diff = diff.add(slotDiffx * .5, 0, slotDiffY * .5);

		BlockPos toTravel = BlockPos.containing(diff.scale(2)
			.add(0.1, 0.1, 0.1));
		Direction lastDirection = null;
		Direction currentDirection = Direction.SOUTH;

		for (int i = 0; i < 100; i++) {
			if (toTravel.equals(BlockPos.ZERO))
				break;

			for (Direction d : Iterate.horizontalDirections) {
				if (lastDirection != null && d == lastDirection.getOpposite())
					continue;
				if (toTravel.relative(d)
					.distManhattan(BlockPos.ZERO) < toTravel.relative(currentDirection)
						.distManhattan(BlockPos.ZERO))
					currentDirection = d;
			}

			lastDirection = currentDirection;
			toTravel = toTravel.relative(currentDirection);
			path.add(currentDirection);
		}

		return path;
	}

}
