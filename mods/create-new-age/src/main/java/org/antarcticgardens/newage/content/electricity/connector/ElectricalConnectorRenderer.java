package org.antarcticgardens.newage.content.electricity.connector;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.newage.content.electricity.wire.ElectricWireItem;
import org.antarcticgardens.newage.content.electricity.wire.WireType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

public class ElectricalConnectorRenderer implements BlockEntityRenderer<ElectricalConnectorBlockEntity> {
    public static final int WIRE_SECTIONS = 24;
    public static final float WIRE_THICKNESS = 0.03f;

    public static final RenderType WIRE = RenderType.create(
            "wire",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LEASH_SHADER)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .createCompositeState(false)
    );

    public ElectricalConnectorRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    public void render(ElectricalConnectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (Map.Entry<BlockPos, WireType> e : blockEntity.getConnectorPositions().entrySet()) {
            if (e.getKey().hashCode() > blockEntity.getBlockPos().hashCode())
                continue;

            poseStack.pushPose();

            VertexConsumer consumer = buffer.getBuffer(WIRE);

            poseStack.translate(0.5f, 0.5f, 0.5f);
            Matrix4f pose = poseStack.last().pose();

            BlockPos pos = blockEntity.getBlockPos();

            Vector3f from = new Vector3f(0.0f);
            Vector3f to = new Vector3f(
                    e.getKey().getX() - pos.getX(),
                    e.getKey().getY() - pos.getY(),
                    e.getKey().getZ() - pos.getZ()
            );

            Vector3f lastSection = from;
            Vector3f direction = new Vector3f(to).sub(from).normalize();
            float distance = to.distance(from);
            float perSection = distance / WIRE_SECTIONS;

            for (int i = 0; i <= WIRE_SECTIONS; i++) {
                int[] color = (i % 2 == 0) ? e.getValue().getColor1() : e.getValue().getColor2();
                Vector3f sectionTo = new Vector3f(direction).mul(perSection * i).add(0.0f, (float) catenary(i, distance), 0.0f);
                wireSection(consumer, pose, lastSection, sectionTo, color, calculateLighting(blockEntity, lastSection), calculateLighting(blockEntity, sectionTo));
                lastSection = sectionTo;
            }

            poseStack.popPose();
        }
    }

    private int calculateLighting(BlockEntity entity, Vector3f pos) {
        BlockPos blockPos = new BlockPos(entity.getBlockPos()).offset(ceil(pos.x()), ceil(pos.y()), ceil(pos.z()));
        int sky = entity.getLevel().getBrightness(LightLayer.SKY, blockPos);
        int block = entity.getLevel().getBrightness(LightLayer.BLOCK, blockPos);
        return LightTexture.pack(block, sky);
    }

    private int ceil(float a) {
        if (a >= 0)
            return (int) Math.ceil(a);
        else
            return (int) -Math.ceil(-a);
    }

    private double catenary(double x, double length) {
        double a = length / ElectricWireItem.MAX_DISTANCE;
        x = (x / WIRE_SECTIONS * 2 - 1);
        return (Math.cosh(x) - Math.cosh(1.0f)) * a;
    }

    private void wireSection(VertexConsumer consumer, Matrix4f pose, Vector3f from, Vector3f to, int[] color, int lightA, int lightB) {
        Vector3f direction = new Vector3f(to).sub(from).normalize();
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

        if (isVertical(direction, up))
            up = new Vector3f(1.0f, 0.0f, 0.0f);

        pose = new Matrix4f(pose)
                .translate(from)
                .rotateTowards(direction, up);

        float a = WIRE_THICKNESS / 2;
        float distance = from.distance(to);

        point(consumer, pose, -a, -a, 0.0f, color, lightA);
        point(consumer, pose, -a, -a, distance, color, lightB);
        point(consumer, pose, a, a, distance, color, lightB);
        point(consumer, pose, a, a, 0.0f, color, lightA);

        point(consumer, pose, a, -a, 0.0f, color, lightA);
        point(consumer, pose, a, -a, distance, color, lightB);
        point(consumer, pose, -a, a, distance, color, lightB);
        point(consumer, pose, -a, a, 0.0f, color, lightA);
    }

    private boolean isVertical(Vector3f vec, Vector3f up) {
        return vec.equals(up, 0.001f) || vec.equals(up.mul(-1.0f), 0.001f);
    }

    private void point(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, int[] color, int light) {
        consumer.vertex(pose, x, y, z).color(color[0], color[1], color[2], color[3]).uv2(light).endVertex();
    }
}
