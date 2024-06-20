package com.simibubi.create.content.kinetics.chainConveyor;

import javax.annotation.Nullable;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class ChainConveyorShape {

	@Nullable
	public abstract Vec3 intersect(Vec3 from, Vec3 to);

	public abstract float getChainPosition(Vec3 intersection);

	public abstract void drawOutline(BlockPos anchor);

	public abstract void drawPoint(BlockPos anchor, float position);

	public static class ChainConveyorOBB extends ChainConveyorShape {

		BlockPos connection;
		double yaw, pitch;
		AABB bounds;
		Vec3 pivot;
		final double radius = 0.25;

		Vec3[] linePoints;

		public ChainConveyorOBB(BlockPos connection, Vec3 start, Vec3 end) {
			this.connection = connection;
			Vec3 diff = end.subtract(start);
			double d = diff.length();
			double dxz = diff.multiply(1, 0, 1)
				.length();
			yaw = Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
			pitch = Mth.RAD_TO_DEG * Mth.atan2(-diff.y, dxz);
			bounds = new AABB(start, start).expandTowards(new Vec3(0, 0, d))
				.inflate(radius, radius, 0);
			pivot = start;
		}

		@Override
		public Vec3 intersect(Vec3 from, Vec3 to) {
			from = counterTransform(from);
			to = counterTransform(to);

			Vec3 result = bounds.clip(from, to)
				.orElse(null);
			if (result == null)
				return null;

			result = transform(result);
			return result;
		}

		private Vec3 counterTransform(Vec3 from) {
			from = from.subtract(pivot);
			from = VecHelper.rotate(from, -yaw, Axis.Y);
			from = VecHelper.rotate(from, -pitch, Axis.X);
			from = from.add(pivot);
			return from;
		}

		private Vec3 transform(Vec3 result) {
			result = result.subtract(pivot);
			result = VecHelper.rotate(result, pitch, Axis.X);
			result = VecHelper.rotate(result, yaw, Axis.Y);
			result = result.add(pivot);
			return result;
		}

		@Override
		public void drawOutline(BlockPos anchor) {
			int key = 0;
			for (double x : new double[] { bounds.minX, bounds.maxX }) {
				for (double y : new double[] { bounds.minY, bounds.maxY }) {
					Vec3 from = transform(new Vec3(x, y, bounds.minZ));
					Vec3 to = transform(new Vec3(x, y, bounds.maxZ));
					from = from.add(Vec3.atLowerCornerOf(anchor));
					to = to.add(Vec3.atLowerCornerOf(anchor));
					CatnipClient.OUTLINER.showLine(Pair.of(Pair.of(anchor, bounds), "c" + key++), from, to)
						.colored(Color.WHITE);
				}
			}
		}

		@Override
		public float getChainPosition(Vec3 intersection) {
			return (float) Math.min(bounds.getZsize(), intersection.distanceTo(pivot));
		}

		@Override
		public void drawPoint(BlockPos anchor, float position) {
			float x = (float) bounds.getCenter().x;
			float y = (float) bounds.getCenter().y;
			Vec3 from = new Vec3(x, y, bounds.minZ);
			Vec3 to = new Vec3(x, y, bounds.maxZ);
			Vec3 point = from.lerp(to, Mth.clamp(position / from.distanceTo(to), 0, 1));
			point = transform(point);
			CatnipClient.OUTLINER.chaseAABB("ChainPointSelection", new AABB(point, point).move(anchor)
				.inflate(0, .175, 0))
				.colored(Color.WHITE)
				.lineWidth(1 / 8f);
		}
	}

	public static class ChainConveyorBB extends ChainConveyorShape {

		Vec3 lb, rb;
		final double radius = 1;
		AABB bounds;

		public ChainConveyorBB(Vec3 center) {
			lb = center.add(0, 0, 0);
			rb = center.add(0, 0.5, 0);
			bounds = new AABB(lb, rb).inflate(radius, 0, radius);
		}

		@Override
		public Vec3 intersect(Vec3 from, Vec3 to) {
			return bounds.clip(from, to)
				.orElse(null);
		}

		@Override
		public void drawOutline(BlockPos anchor) {
			CatnipClient.OUTLINER.showAABB(anchor, bounds.move(anchor))
				.colored(Color.WHITE);
		}

		@Override
		public float getChainPosition(Vec3 intersection) {
			Vec3 diff = bounds.getCenter()
				.subtract(intersection);
			float angle = (float) (Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z) + 360 + 180) % 360;
			return angle;
		}

		@Override
		public void drawPoint(BlockPos anchor, float position) {
			Vec3 point = bounds.getCenter();
			point = point.add(VecHelper.rotate(new Vec3(0, 0, radius), position, Axis.Y));
			CatnipClient.OUTLINER.chaseAABB("ChainPointSelection", new AABB(point, point).move(anchor)
				.inflate(0, .175, 0))
				.colored(Color.WHITE)
				.lineWidth(1 / 8f);
		}

	}

}
