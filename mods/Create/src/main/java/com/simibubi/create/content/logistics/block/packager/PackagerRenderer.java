package com.simibubi.create.content.logistics.block.packager;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PackagerRenderer extends SmartBlockEntityRenderer<PackagerBlockEntity> {

	public PackagerRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(PackagerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		ItemStack renderedBox = be.getRenderedBox();
		float trayOffset = be.getTrayOffset(partialTicks);
		boolean hatchOpen = be.animationTicks > 5 && be.animationTicks < PackagerBlockEntity.CYCLE - 5;
		BlockState blockState = be.getBlockState();
		Direction facing = blockState.getValue(PackagerBlock.FACING)
			.getOpposite();

		PartialModel hatchModel =
			hatchOpen ? AllPartialModels.PACKAGER_HATCH_OPEN : AllPartialModels.PACKAGER_HATCH_CLOSED;

		SuperByteBuffer sbb = CachedBufferer.partial(hatchModel, blockState);
		sbb.translate(Vec3.atLowerCornerOf(facing.getNormal())
			.scale(.49999f))
			.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
			.rotateCentered(Direction.EAST, AngleHelper.rad(AngleHelper.verticalAngle(facing)))
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		ms.pushPose();
		TransformStack msr = TransformStack.cast(ms);
		msr.translate(Vec3.atLowerCornerOf(facing.getNormal())
			.scale(trayOffset));

		sbb = CachedBufferer.partial(AllPartialModels.PACKAGER_TRAY, blockState);
		sbb.rotateCentered(Direction.UP, AngleHelper.rad(facing.toYRot()))
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		if (!renderedBox.isEmpty()) {
			msr.translate(.5f, .5f, .5f)
				.rotateY(facing.toYRot())
				.translate(0, 2 / 16f, 0)
				.scale(1.49f, 1.49f, 1.49f);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderStatic(null, renderedBox, TransformType.FIXED, false, ms, buffer, be.getLevel(), light, overlay,
					0);
		}

		ms.popPose();
	}

}
