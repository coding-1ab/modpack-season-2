package com.simibubi.create.content.logistics.factoryBoard;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class FactoryPanelRenderer extends SmartBlockEntityRenderer<FactoryPanelBlockEntity> {

	public FactoryPanelRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FactoryPanelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		for (FactoryPanelBehaviour behaviour : be.panels.values()) {
			if (!behaviour.isActive())
				continue;
			if (behaviour.getAmount() > 0)
				renderBulb(behaviour, partialTicks, ms, buffer, light, overlay);
			for (FactoryPanelConnection connection : behaviour.targetedBy.values())
				renderPath(behaviour, connection, partialTicks, ms, buffer, light, overlay);
		}
	}

	public static void renderBulb(FactoryPanelBehaviour behaviour, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		BlockState blockState = behaviour.blockEntity.getBlockState();

		float xRot = FactoryPanelBlock.getXRot(blockState) + Mth.PI / 2;
		float yRot = FactoryPanelBlock.getYRot(blockState);
		float glow = behaviour.bulb.getValue(partialTicks);

		CachedBuffers.partial(AllPartialModels.FACTORY_PANEL_LIGHT, blockState)
			.rotateCentered(yRot, Direction.UP)
			.rotateCentered(xRot, Direction.EAST)
			.rotateCentered(Mth.PI, Direction.UP)
			.translate(behaviour.slot.xOffset * .5, 0, behaviour.slot.yOffset * .5)
			.light(glow > 0.125f ? LightTexture.FULL_BRIGHT : light)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderType.translucent()));

		if (glow < .125f)
			return;

		glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
		glow = Mth.clamp(glow, -1, 1);
		int color = (int) (200 * glow);

		CachedBuffers.partial(AllPartialModels.FACTORY_PANEL_LIGHT, blockState)
			.rotateCentered(yRot, Direction.UP)
			.rotateCentered(xRot, Direction.EAST)
			.rotateCentered(Mth.PI, Direction.UP)
			.translate(behaviour.slot.xOffset * .5, 0, behaviour.slot.yOffset * .5)
//			.translate(1 / 16f, 2 / 16f, 7 / 16f)
//			.scale(1.25f)
//			.translate(-1 / 16f, -2 / 16f, -7 / 16f)
			.light(LightTexture.FULL_BRIGHT)
			.color(color, color, color, 255)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
	}

	public static void renderPath(FactoryPanelBehaviour behaviour, FactoryPanelConnection connection,
		float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		BlockState blockState = behaviour.blockEntity.getBlockState();
		List<Direction> path = connection.getPath(blockState, behaviour.getPanelPosition());

		float xRot = FactoryPanelBlock.getXRot(blockState) + Mth.PI / 2;
		float yRot = FactoryPanelBlock.getYRot(blockState);
		float glow = behaviour.bulb.getValue(partialTicks);
		float yOffset = 0;

		boolean success = connection.success;

		int color = behaviour.waitingForNetwork ? 0x5B3B3B
			: behaviour.satisfied ? 0x9EFF7F : behaviour.promisedSatisfied ? 0x7FD6DB : 0x708DAD;
		yOffset = behaviour.promisedSatisfied ? 1 : behaviour.satisfied ? 0 : 2;

		if (!behaviour.waitingForNetwork && glow > 0 && !behaviour.satisfied) {
			color = Color.mixColors(color, success ? 0xEAF2EC : 0xE5654B, glow);
			if (!behaviour.satisfied && !behaviour.promisedSatisfied)
				yOffset += (success ? 1 : 2) * glow;
		}

		float currentX = 0;
		float currentZ = 0;

		for (int i = 0; i < path.size(); i++) {
			Direction direction = path.get(i);

			currentX += direction.getStepX() * .5;
			currentZ += direction.getStepZ() * .5;

			SuperByteBuffer connectionSprite = CachedBuffers
				.partial((i == 0 ? AllPartialModels.FACTORY_PANEL_ARROWS : AllPartialModels.FACTORY_PANEL_LINES)
					.get(direction.getOpposite()), blockState)
				.rotateCentered(yRot, Direction.UP)
				.rotateCentered(xRot, Direction.EAST)
				.rotateCentered(Mth.PI, Direction.UP)
				.translate(behaviour.slot.xOffset * .5 + .25, 0, behaviour.slot.yOffset * .5 + .25)
				.translate(currentX, (yOffset + (direction.get2DDataValue() % 2) * 0.125f) / 512f, currentZ);

			if (!behaviour.waitingForNetwork && !behaviour.satisfied && glow < 0.25)
				connectionSprite.shiftUV(AllSpriteShifts.FACTORY_PANEL_CONNECTIONS);

			connectionSprite.color(color)
				.light(light)
				.overlay(overlay)
				.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
		}

	}

}
