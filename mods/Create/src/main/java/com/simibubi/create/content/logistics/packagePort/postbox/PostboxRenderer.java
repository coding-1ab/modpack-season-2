package com.simibubi.create.content.logistics.packagePort.postbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.Mth;

public class PostboxRenderer extends SmartBlockEntityRenderer<PostboxBlockEntity> {

	public PostboxRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(PostboxBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {

		if (blockEntity.addressFilter != null && !blockEntity.addressFilter.isBlank())
			renderNameplateOnHover(blockEntity, Components.literal(blockEntity.addressFilter), 1, ms, buffer, light);

		CachedBuffers.partial(AllPartialModels.POSTBOX_FLAG, blockEntity.getBlockState())
			.light(light)
			.overlay(overlay)
			.rotateCentered(Mth.DEG_TO_RAD * (180 - blockEntity.getBlockState()
				.getValue(PostboxBlock.FACING)
				.toYRot()), Axis.YP)
			.renderInto(ms, buffer.getBuffer(RenderType.cutout()));
	}

}
