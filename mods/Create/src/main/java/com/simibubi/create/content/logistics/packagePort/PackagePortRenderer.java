package com.simibubi.create.content.logistics.packagePort;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class PackagePortRenderer extends SmartBlockEntityRenderer<PackagePortBlockEntity> {

	public PackagePortRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(PackagePortBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

		for (int i = 0; i < 3; i++) {
			SuperByteBuffer tube =
				CachedBuffers.partial(AllPartialModels.PACKAGE_PORT_TUBE, blockEntity.getBlockState());
			tube.light(light)
				.translate(0, (i * 3 + 8.5f) / 16f, 0)
				.overlay(overlay)
				.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}

		SuperByteBuffer cap = CachedBuffers.partial(AllPartialModels.PACKAGE_PORT_CAP, blockEntity.getBlockState());
		cap.light(light)
			.translate(0, 15 / 16f, 0)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}

}
