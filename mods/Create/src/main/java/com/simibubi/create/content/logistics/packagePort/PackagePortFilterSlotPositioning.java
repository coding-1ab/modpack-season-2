package com.simibubi.create.content.logistics.packagePort;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PackagePortFilterSlotPositioning extends ValueBoxTransform {

	@Override
	public boolean shouldRender(LevelAccessor level, BlockPos pos, BlockState state) {
		if (!super.shouldRender(level, pos, state))
			return false;
		if (level != null && level.getBlockEntity(pos) instanceof PackagePortBlockEntity ppbe)
			return !ppbe.isAnimationInProgress();
		return true;
	}

	@Override
	public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
		float horizontalAngle = 0;
		if (level != null && level.getBlockEntity(pos) instanceof PackagePortBlockEntity ppbe)
			horizontalAngle = ppbe.getYaw();

		Vec3 southLocation = VecHelper.voxelSpace(8, 13.5, 10);
		return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
	}

	@Override
	public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
		float horizontalAngle = 0;
		if (level != null && level.getBlockEntity(pos) instanceof PackagePortBlockEntity ppbe)
			horizontalAngle = ppbe.getYaw();

		TransformStack.of(ms)
			.rotateY(horizontalAngle)
			.rotateX(90);
	}

}
