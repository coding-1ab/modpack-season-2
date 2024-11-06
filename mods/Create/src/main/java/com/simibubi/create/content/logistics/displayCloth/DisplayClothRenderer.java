package com.simibubi.create.content.logistics.displayCloth;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DisplayClothRenderer extends SmartBlockEntityRenderer<DisplayClothBlockEntity> {

	public DisplayClothRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(DisplayClothBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
		List<ItemStack> stacks = blockEntity.getItemsForRender();

		for (int i = 0; i < stacks.size(); i++) {
			ItemStack entry = stacks.get(i);
			ms.pushPose();
			ms.translate(0.5f, 3 / 16f, 0.5f);

			if (stacks.size() > 1) {
				ms.mulPose(Axis.YP.rotationDegrees(i * (360f / stacks.size()) + 45f));
				ms.translate(0, i % 2 == 0 ? -0.005 : 0, 5 / 16f);
				ms.mulPose(Axis.YP.rotationDegrees(-i * (360f / stacks.size()) - 45f));
			}

			ms.mulPose(Axis.YP.rotationDegrees(180));
			DepotRenderer.renderItem(blockEntity.getLevel(), ms, buffer, light, OverlayTexture.NO_OVERLAY, entry, 0,
				null, Vec3.atCenterOf(blockEntity.getBlockPos()), true);
			ms.popPose();
		}
	}

}
