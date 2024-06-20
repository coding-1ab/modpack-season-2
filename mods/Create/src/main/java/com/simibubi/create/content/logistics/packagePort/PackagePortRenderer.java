package com.simibubi.create.content.logistics.packagePort;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class PackagePortRenderer extends SmartBlockEntityRenderer<PackagePortBlockEntity> {

	public PackagePortRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(PackagePortBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

		SuperByteBuffer body = CachedBuffers.partial(AllPartialModels.PACKAGE_PORT_BODY, blockEntity.getBlockState());

		float yaw = blockEntity.passiveYaw;
		float headPitch = 80;
		float tonguePitch = 0;
		float tongueLength = 0;

		Vec3 diff = Vec3.ZERO;

		if (blockEntity.target != null) {
			diff = blockEntity.target
				.getExactTargetLocation(blockEntity, blockEntity.getLevel(), blockEntity.getBlockPos())
				.subtract(0, 1, 0)
				.subtract(Vec3.atCenterOf(blockEntity.getBlockPos()));
			yaw = (float) (Mth.atan2(diff.x, diff.z) * Mth.RAD_TO_DEG) + 180;
			tonguePitch = (float) Mth.atan2(diff.y, diff.multiply(1, 0, 1)
				.length() + (3 / 16f)) * Mth.RAD_TO_DEG;
			tongueLength = Math.max((float) diff.length(), 1);
			headPitch = Mth.clamp(tonguePitch * 2, 60, 100);
		}

		if (blockEntity.isAnimationInProgress()) {
			float progress = blockEntity.animationProgress.getValue(partialTicks);
			float scale = 1;
			float itemDistance = 0;

			if (blockEntity.currentlyDepositing) {
				double modifier = Math.max(0, 1 - Math.pow((progress - 0.25) * 4 - 1, 4));
				itemDistance =
					(float) Math.max(tongueLength * Math.min(1, (progress - 0.25) * 3), tongueLength * modifier);
				tongueLength *= modifier;
				headPitch *= Math.max(0, 1 - Math.pow((progress) * 2 - 1, 4));
				scale = 0.5f + progress / 2;

			} else {
				tongueLength *= Math.pow(Math.max(0, 1 - progress * 1.25), 5);
				headPitch *= Math.min(progress * 30, 1 - Math.max(0, (progress - 0.5) * 2));
				scale = (float) Math.max(0.5, 1 - progress * 1.25);
				itemDistance = tongueLength;
			}

			renderPackage(blockEntity, ms, buffer, light, overlay, diff, scale, itemDistance);
		} else {
			tongueLength = 0;
			headPitch = 0;
		}

		body.centre()
			.rotateY(yaw)
			.unCentre()
			.light(light)
//			.color(color)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		SuperByteBuffer head = CachedBuffers.partial(AllPartialModels.PACKAGE_PORT_HEAD, blockEntity.getBlockState());

		head.centre()
			.rotateY(yaw)
			.unCentre()
			.translate(8 / 16f, 10 / 16f, 11 / 16f)
			.rotateX(headPitch)
			.translateBack(8 / 16f, 10 / 16f, 11 / 16f);

		head.light(light)
//			.color(color)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		SuperByteBuffer tongue =
			CachedBuffers.partial(AllPartialModels.PACKAGE_PORT_TONGUE, blockEntity.getBlockState());

		tongue.centre()
			.rotateY(yaw)
			.unCentre()
			.translate(8 / 16f, 10 / 16f, 11 / 16f)
			.rotateX(tonguePitch)
			.scale(1f, 1f, tongueLength / (7 / 16f))
			.translateBack(8 / 16f, 10 / 16f, 11 / 16f);

		tongue.light(light)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		// hat

//		SuperByteBuffer hatBuffer = CachedBuffers.partial(AllPartialModels.TRAIN_HAT, blockEntity.getBlockState());
//		hatBuffer
//			.translate(8 / 16f, 14 / 16f, 8 / 16f)
//			.rotateY(yaw + 180)
//			.translate(0, 0, -3 / 16f)
//			.rotateX(-4)
//			.translateBack(0, 0, -3 / 16f)
//			.translate(0, 0, 1 / 16f)
//			.light(light)
//			.color(color)
//			.overlay(overlay)
//			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

	}

	private void renderPackage(PackagePortBlockEntity blockEntity, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay, Vec3 diff, float scale, float itemDistance) {
		if (blockEntity.animatedPackage == null)
			return;
		ResourceLocation key = ForgeRegistries.ITEMS.getKey(blockEntity.animatedPackage.getItem());
		if (key == null)
			return;
		SuperByteBuffer rigBuffer =
			CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(key), blockEntity.getBlockState());
		SuperByteBuffer boxBuffer =
			CachedBuffers.partial(AllPartialModels.PACKAGES.get(key), blockEntity.getBlockState());
		for (SuperByteBuffer buf : new SuperByteBuffer[] { boxBuffer, rigBuffer }) {
			buf.translate(0, 3 / 16f, 0)
				.translate(diff.normalize()
					.scale(itemDistance))
				.centre()
				.scale(scale)
				.unCentre()
				.light(light)
				.overlay(overlay)
				.renderInto(ms, buffer.getBuffer(RenderType.cutout()));
			if (!blockEntity.currentlyDepositing)
				break;
		}
	}

}
