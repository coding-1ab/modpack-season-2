package com.simibubi.create.content.logistics.orderCollector;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class OrderCollectorRenderer extends KineticBlockEntityRenderer<OrderCollectorBlockEntity> {

	public OrderCollectorRenderer(Context context) {
		super(context);
	}

	@Override
	public boolean shouldRenderOffScreen(OrderCollectorBlockEntity be) {
		return true;
	}

	@Override
	protected void renderSafe(OrderCollectorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		ItemStack box = be.visuallyCollectingPackage;
		boolean collecting = !box.isEmpty();
		if (!collecting) {
			box = be.visuallyDeployingPackage;
			if (box.isEmpty())
				return;
		}

		double progress = 0;

		if (collecting)
			progress = Mth.clamp((be.collectingAnimationTicks + partialTicks) / 20f, 0, 1);
		else
			progress = 1 - Mth.clamp((be.deployingAnimationTicks + partialTicks) / 10f, 0, 1);

		double offset = Mth.lerp(1 - Math.pow(1 - progress, 4), -1.5, -0.26);
		float scale = (float) Mth.lerp(1 - Math.pow(1 - progress, 2), 2, 1.5);

		TransformStack msr = TransformStack.cast(ms);
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		ms.pushPose();
		msr.centre();
		ms.translate(0, 6 / 16f, 0);
		ms.translate(0, offset, 0);
		ms.scale(scale, scale, scale);

		itemRenderer.renderStatic(box, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);

		ms.popPose();

	}

	@Override
	protected BlockState getRenderedBlockState(OrderCollectorBlockEntity be) {
		return shaft(getRotationAxisOf(be));
	}

}
