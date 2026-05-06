package dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.creative_vector_thruster.CreativeVectorThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public final class VectorThrusterRenderer {
    private static final float PIVOT_X = 7.0f / 16.0f;
    private static final float PIVOT_Y = 9.0f / 16.0f;
    private static final float PIVOT_Z = 1.0f / 16.0f;

    private VectorThrusterRenderer() {
    }

    public static void render(VectorThrusterBlockEntity be, float partialTick, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (be == null || be.isRemoved()) {
            return;
        }

        BlockState state = be.getBlockState();
        if (!state.hasProperty(AbstractThrusterBlock.FACING)) {
            return;
        }
        Direction facing = state.getValue(AbstractThrusterBlock.FACING);

        float xAxis = Mth.clamp(be.getInterpolatedVectorX(partialTick), -1.0f, 1.0f);
        float yAxis = Mth.clamp(be.getInterpolatedVectorY(partialTick), -1.0f, 1.0f);
        float yawDegrees = xAxis * VectorThrusterBlockEntity.MAX_VISUAL_TILT_DEGREES;
        float pitchDegrees = -yAxis * VectorThrusterBlockEntity.MAX_VISUAL_TILT_DEGREES;
        // For FACING=UP the Rx(90°) facing rotation maps model-Y to world +Z, inverting
        // the effective yaw/pitch axis relative to computeRight's negated right/up vectors.
        if (facing == Direction.UP) {
            yawDegrees = -yawDegrees;
            pitchDegrees = -pitchDegrees;
        }

        PartialModel partialModel = be instanceof CreativeVectorThrusterBlockEntity
                ? PropulsionPartialModels.CREATIVE_VECTOR_THRUSTER_MOVING_ASSEMBLY
                : PropulsionPartialModels.VECTOR_THRUSTER_MOVING_ASSEMBLY;
        SuperByteBuffer movingAssembly = CachedBuffers.partial(partialModel, state);
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        applyFacingRotation(ms, facing);
        ms.translate(-0.5, -0.5, -0.5);

        ms.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
        ms.mulPose(Axis.YP.rotationDegrees(yawDegrees));
        ms.mulPose(Axis.XP.rotationDegrees(pitchDegrees));
        ms.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);

        movingAssembly.light(light).overlay(overlay).renderInto(ms, vb);
        ms.popPose();
    }

    private static void applyFacingRotation(PoseStack ms, Direction facing) {
        switch (facing) {
            case NORTH -> {
            }
            case SOUTH -> ms.mulPose(Axis.YP.rotationDegrees(-180));
            case WEST -> ms.mulPose(Axis.YP.rotationDegrees(-270));
            case EAST -> ms.mulPose(Axis.YP.rotationDegrees(-90));
            case UP -> ms.mulPose(Axis.XP.rotationDegrees(-270));
            case DOWN -> ms.mulPose(Axis.XP.rotationDegrees(-90));
        }
    }
}
