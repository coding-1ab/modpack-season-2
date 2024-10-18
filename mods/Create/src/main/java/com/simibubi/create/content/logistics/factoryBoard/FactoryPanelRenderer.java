package com.simibubi.create.content.logistics.factoryBoard;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.Pointing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelRenderer extends SmartBlockEntityRenderer<FactoryPanelBlockEntity> {

	public FactoryPanelRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FactoryPanelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		be.connections.forEach((fromSide, behaviour) -> {
			if (behaviour.inputModeActive)
				renderAttachment(be, fromSide, true, ms, buffer, light, overlay);
		});

		be.inboundConnections.forEach((toSide, map) -> {
			map.forEach((fromPos, connection) -> renderConnection(be.getBlockState(), fromPos.offset(be.getBlockPos()),
				be.getBlockPos(), connection.fromSide(), toSide, be.satisfied, false));
			renderAttachment(be, toSide, false, ms, buffer, light, overlay);
		});
	}

	private void renderAttachment(FactoryPanelBlockEntity be, Pointing side, boolean input, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		ms.pushPose();

		float offset = 6 / 16f;

		CachedBuffers
			.partial(input ? AllPartialModels.FACTORY_PANEL_INPUT : AllPartialModels.FACTORY_PANEL_TIMER,
				be.getBlockState())
			.rotateCentered(be.getYRot() + Mth.PI, Direction.UP)
			.rotateCentered(-be.getXRot(), Direction.EAST)
			.translate(0, side == Pointing.UP ? offset : side == Pointing.DOWN ? -offset : 0, 0)
			.translate(side == Pointing.LEFT ? offset : side == Pointing.RIGHT ? -offset : 0, 0, 0)
			.light(light)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		ms.popPose();
	}

	public static void renderConnection(BlockState blockState, BlockPos fromPos, BlockPos toPos, Pointing fromSide,
		Pointing toSide, boolean satisfied, boolean effect) {
		Direction facing = FactoryPanelBlock.connectedDirection(blockState);
		Vec3 facingNormal = Vec3.atLowerCornerOf(facing.getNormal());
		Vec3 offset = Vec3.atCenterOf(BlockPos.ZERO)
			.add(facingNormal.scale(-0.375));

		int color = satisfied ? 0x85E59B : (AnimationTickHolder.getTicks() % 16 >= 8) ? 0x7783A8 : 0x687291;

		if (effect)
			color = satisfied ? 0xEAF2EC : 0xE5654B;

		Pointing currentDirection = fromSide;
		BlockPos currentPos = fromPos;
		BlockPos targetPos = toPos.relative(FactoryPanelBlock.getDirection(blockState, toSide));

		for (int i = 0; i < 100; i++) {
			BlockPos nextPos = currentPos.relative(FactoryPanelBlock.getDirection(blockState, currentDirection));

			Vec3 fromOffset = Vec3.atLowerCornerOf(currentPos)
				.add(offset);
			Vec3 toOffset = Vec3.atLowerCornerOf(nextPos)
				.add(offset);

			Pair<Integer, Pair<Boolean, Boolean>> key =
				Pair.of(currentPos.hashCode() + 13 * nextPos.hashCode(), Pair.of(effect, effect ? satisfied : true));

			CatnipClient.OUTLINER.showLine(key, fromOffset, toOffset)
				.lineWidth(effect ? (satisfied ? 3f / 32f : 3.5f / 32f) : 2 / 32f)
				.colored(color);

			if (currentPos.equals(targetPos))
				break;

			currentPos = nextPos;

			if (currentPos.equals(targetPos)) {
				currentDirection = Pointing.values()[(toSide.ordinal() + 2) % 4];
				continue;
			}

			for (Pointing p : Pointing.values()) {
				if (p != currentDirection && Math.abs(p.ordinal() - currentDirection.ordinal()) % 2 == 0)
					continue;
				if (currentPos.relative(FactoryPanelBlock.getDirection(blockState, p))
					.distManhattan(targetPos) < currentPos
						.relative(FactoryPanelBlock.getDirection(blockState, currentDirection))
						.distManhattan(targetPos))
					currentDirection = p;
			}

		}

	}

}
