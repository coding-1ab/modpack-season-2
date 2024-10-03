package com.simibubi.create.content.logistics.displayCloth;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.depot.DepotRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.IntAttached;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class DisplayClothRenderer extends EntityRenderer<DisplayClothEntity> {

	public DisplayClothRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(DisplayClothEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer,
		int light) {
		PartialModel partialModel = AllPartialModels.DISPLAY_CLOTH;
		SuperByteBuffer sbb = CachedBuffers.partial(partialModel, Blocks.AIR.defaultBlockState());

		sbb.rotateYDegrees(180 + -entity.getYRot())
			.translate(-.5, 0, -.5)
			.light(light)
			.renderInto(ms, buffer.getBuffer(Sheets.solidBlockSheet()));

		if (entity.requestData == null) {
			super.render(entity, yaw, pt, ms, buffer, light);
			return;
		}

		List<IntAttached<ItemStack>> stacks = entity.requestData.encodedRequest.stacks();

		for (int i = 0; i < stacks.size(); i++) {
			IntAttached<ItemStack> entry = stacks.get(i);
			ms.pushPose();
			ms.translate(0, 3 / 16f, 0);

			if (stacks.size() > 1) {
				ms.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() + i * (360f / stacks.size())));
				ms.translate(0, i % 2 == 0 ? -0.005 : 0, 4 / 16f);
				ms.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - i * (360f / stacks.size())));
			}

			ms.mulPose(Axis.YP.rotationDegrees(180));
			DepotRenderer.renderItem(entity.level(), ms, buffer, light, OverlayTexture.NO_OVERLAY, entry.getSecond(), 0,
				null, entity.position(), true);
			ms.popPose();
		}

		super.render(entity, yaw, pt, ms, buffer, light);
	}

	@Override
	public ResourceLocation getTextureLocation(DisplayClothEntity entity) {
		return null;
	}

}
