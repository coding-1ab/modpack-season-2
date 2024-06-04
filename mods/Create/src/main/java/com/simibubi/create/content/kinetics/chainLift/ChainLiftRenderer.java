package com.simibubi.create.content.kinetics.chainLift;

import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftBlockEntity.ConnectionStats;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftPackage.ChainLiftPackagePhysicsData;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.ponder.utility.LevelTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChainLiftRenderer extends KineticBlockEntityRenderer<ChainLiftBlockEntity> {

	public ChainLiftRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ChainLiftBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		BlockPos pos = be.getBlockPos();

		renderChains(be, ms, buffer, light, overlay);

		for (ChainLiftPackage box : be.loopingPackages)
			renderBox(be, ms, buffer, overlay, pos, box, partialTicks);
		for (Entry<BlockPos, List<ChainLiftPackage>> entry : be.travellingPackages.entrySet())
			for (ChainLiftPackage box : entry.getValue())
				renderBox(be, ms, buffer, overlay, pos, box, partialTicks);
	}

	private void renderBox(ChainLiftBlockEntity be, PoseStack ms, MultiBufferSource buffer, int overlay, BlockPos pos,
		ChainLiftPackage box, float partialTicks) {
		if (box.worldPosition == null)
			return;

		ChainLiftPackagePhysicsData physicsData = box.physicsData(be.getLevel());

		ms.pushPose();

		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		Vec3 position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
		ms.translate(position.x - pos.getX(), position.y - pos.getY(), position.z - pos.getZ());
		ms.scale(2f, 2f, 2f);

		BlockPos containingPos = BlockPos.containing(position);
		Level level = be.getLevel();
		itemRenderer.renderStatic(null, box.item, ItemDisplayContext.FIXED, false, ms, buffer, level,
			LightTexture.pack(level.getBrightness(LightLayer.BLOCK, containingPos),
				level.getBrightness(LightLayer.SKY, containingPos)),
			overlay, 0);

		ms.popPose();
	}

	private void renderChains(ChainLiftBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		float time = LevelTickHolder.getRenderTime(be.getLevel()) / (180f / Math.abs(be.getSpeed()));
		time %= 1;
		if (time < 0)
			time += 1;

		float animation = time - 0.5f;

		for (BlockPos blockPos : be.connections) {
			ConnectionStats stats = be.connectionStats.get(blockPos);
			if (stats == null)
				continue;

			Vec3 diff = stats.end()
				.subtract(stats.start());
			double yaw = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
			double pitch = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.y, diff.multiply(1, 0, 1)
				.length());

			ms.pushPose();

			Vec3 startOffset = stats.start()
				.subtract(Vec3.atCenterOf(be.getBlockPos()));

			SuperByteBuffer guard = CachedBuffers.partial(AllPartialModels.CHAIN_LIFT_GUARD, be.getBlockState());
			guard.translate(startOffset.multiply(0, 1, 0));
			guard.translate(0.5, 0.5, 0.5);
			guard.rotateY(yaw);

			guard.translate(0, 0, 11 / 16f);
			guard.rotateX(-pitch);
			guard.translate(0, 0, -11 / 16f);

			guard.translate(-0.5, 0, -.25);
			guard.light(light)
				.nudge((int) blockPos.asLong())
				.overlay(overlay)
				.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

			double segments = stats.chainLength();
			int roundedSegments = (int) Math.round(segments);
			float scale = (float) (segments / roundedSegments);

			for (int i = 0; i < roundedSegments; i++) {
				SuperByteBuffer chain = CachedBuffers.block(Blocks.CHAIN.defaultBlockState());
				chain.centre();
				chain.translate(startOffset);
				chain.rotateY(yaw);
				chain.rotateX(90 - pitch);
				chain.translate(0, 8 / 16f, 0);
				chain.unCentre();
				chain.scale(1, scale, 1);
				chain.translate(0, i + animation, 0);

				chain.light(light)
					.overlay(overlay)
					.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
			}

			ms.popPose();
		}
	}

	@Override
	public boolean shouldRenderOffScreen(ChainLiftBlockEntity be) {
		return !be.connections.isEmpty();
	}

	@Override
	protected BlockState getRenderedBlockState(ChainLiftBlockEntity be) {
		return shaft(Axis.Y);
	}

}
