package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class ChainConveyorRidingHandler {

	public static BlockPos ridingChainConveyor;
	public static float chainPosition;
	public static BlockPos ridingConnection;
	public static boolean flipped;

	public static void embark(BlockPos lift, float position, BlockPos connection) {
		ridingChainConveyor = lift;
		chainPosition = position;
		ridingConnection = connection;
		if (Minecraft.getInstance().level.getBlockEntity(ridingChainConveyor) instanceof ChainConveyorBlockEntity clbe)
			flipped = clbe.getSpeed() < 0;
	}

	public static void clientTick() {
		if (ridingChainConveyor == null)
			return;
		Minecraft mc = Minecraft.getInstance();
		BlockEntity blockEntity = mc.level.getBlockEntity(ridingChainConveyor);
		if (mc.player.isCrouching() || !(blockEntity instanceof ChainConveyorBlockEntity clbe)) {
			ridingChainConveyor = null;
			ridingConnection = null;
			return;
		}
		if (ridingConnection != null && !clbe.connections.contains(ridingConnection)) {
			ridingChainConveyor = null;
			ridingConnection = null;
			return;
		}

		clbe.prepareStats();

		Vec3 playerPosition = mc.player.position()
			.add(0, mc.player.getBoundingBox()
				.getYsize() + 0.75, 0);

		updateTargetPosition(mc, clbe);

		blockEntity = mc.level.getBlockEntity(ridingChainConveyor);
		if (!(blockEntity instanceof ChainConveyorBlockEntity))
			return;

		clbe = (ChainConveyorBlockEntity) blockEntity;
		clbe.prepareStats();

		Vec3 targetPosition = playerPosition;

		if (ridingConnection != null) {
			ConnectionStats stats = clbe.connectionStats.get(ridingConnection);
			targetPosition = stats.start()
				.add((stats.end()
					.subtract(stats.start())).normalize()
						.scale(Math.min(stats.chainLength(), chainPosition)));
		} else {
			targetPosition = Vec3.atBottomCenterOf(ridingChainConveyor)
				.add(VecHelper.rotate(new Vec3(0, 0.25, 0.875), chainPosition, Axis.Y));
		}

		Vec3 diff = targetPosition.subtract(playerPosition);
		if (diff.length() > 3) {
			ridingChainConveyor = null;
			ridingConnection = null;
			return;
		}

		mc.player.setDeltaMovement(mc.player.getDeltaMovement()
			.scale(0.75)
			.add(diff.scale(0.25)));
		if (AnimationTickHolder.getTicks() % 10 == 0)
			AllPackets.getChannel()
				.sendToServer(new ChainConveyorRidingPacket(ridingChainConveyor));
	}

	private static void updateTargetPosition(Minecraft mc, ChainConveyorBlockEntity clbe) {
		float serverSpeed = ServerSpeedProvider.get();
		float speed = clbe.getSpeed() / 360f;
		float radius = 1.5f;
		float distancePerTick = Math.abs(speed);
		float degreesPerTick = (speed / (Mth.PI * radius)) * 360f;

		if (ridingConnection != null) {
			ConnectionStats stats = clbe.connectionStats.get(ridingConnection);

			if (flipped != clbe.getSpeed() < 0) {
				flipped = clbe.getSpeed() < 0;
				ridingChainConveyor = clbe.getBlockPos()
					.offset(ridingConnection);
				chainPosition = stats.chainLength() - chainPosition;
				ridingConnection = ridingConnection.multiply(-1);
				return;
			}

			chainPosition += serverSpeed * distancePerTick;
			chainPosition = Math.min(stats.chainLength(), chainPosition);
			if (chainPosition < stats.chainLength())
				return;

			// transfer to other
			if (mc.level.getBlockEntity(clbe.getBlockPos()
				.offset(ridingConnection)) instanceof ChainConveyorBlockEntity clbe2) {
				chainPosition = clbe.wrapAngle(stats.tangentAngle() + 180 + 2 * 35 * (clbe.reversed ? -1 : 1));
				ridingChainConveyor = clbe2.getBlockPos();
				ridingConnection = null;
			}

			return;
		}

		float prevChainPosition = chainPosition;
		chainPosition += serverSpeed * degreesPerTick;
		chainPosition = clbe.wrapAngle(chainPosition);

		BlockPos nearestLooking = BlockPos.ZERO;
		double bestDiff = Double.MAX_VALUE;
		for (BlockPos connection : clbe.connections) {
			double diff = Vec3.atLowerCornerOf(connection)
				.normalize()
				.distanceToSqr(mc.player.getLookAngle()
					.normalize());
			if (diff > bestDiff)
				continue;
			nearestLooking = connection;
			bestDiff = diff;
		}

		if (nearestLooking == BlockPos.ZERO)
			return;

		float offBranchAngle = clbe.connectionStats.get(nearestLooking)
			.tangentAngle();
		if (!clbe.loopThresholdCrossed(chainPosition, prevChainPosition, offBranchAngle))
			return;

		chainPosition = 0;
		ridingConnection = nearestLooking;
	}

}
