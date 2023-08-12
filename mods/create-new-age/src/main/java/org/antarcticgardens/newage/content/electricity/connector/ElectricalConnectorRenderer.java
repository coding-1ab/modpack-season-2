package org.antarcticgardens.newage.content.electricity.connector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.newage.NewAgeRenderTypes;
import org.antarcticgardens.newage.content.electricity.wire.ElectricWireItem;
import org.antarcticgardens.newage.content.electricity.wire.WireType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

public class ElectricalConnectorRenderer implements BlockEntityRenderer<ElectricalConnectorBlockEntity> {
    public static final int WIRE_SECTIONS_PER_METER = 10;
    public static final float WIRE_THICKNESS = 0.03f;

    public ElectricalConnectorRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    public void render(ElectricalConnectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (Map.Entry<BlockPos, WireType> e : blockEntity.getConnectorPositions().entrySet()) {
            if (e.getKey().hashCode() > blockEntity.getBlockPos().hashCode())
                continue;

            poseStack.pushPose();

            VertexConsumer consumer = buffer.getBuffer(NewAgeRenderTypes.WIRE);

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
            int sections = (int) Math.ceil(distance * WIRE_SECTIONS_PER_METER);
            float perSection = distance / sections;

            for (int i = 0; i <= sections; i++) {
                int[] color = (i % 2 == 0) ? e.getValue().getColor1() : e.getValue().getColor2();
                Vector3f sectionTo = new Vector3f(direction).mul(perSection * i).add(0.0f, catenary(i, distance, sections), 0.0f);
                wireSection(consumer, pose, lastSection, sectionTo, color, calculateLighting(blockEntity, lastSection, sectionTo));
                lastSection = sectionTo;
            }

            poseStack.popPose();
        }
    }

    private int calculateLighting(BlockEntity entity, Vector3f pos, Vector3f pos1) {
        BlockPos blockPos = new BlockPos(entity.getBlockPos()).offset(Math.round(pos.x()), Math.round(pos.y()), Math.round(pos.z()));
        BlockPos blockPos1 = new BlockPos(entity.getBlockPos()).offset(Math.round(pos1.x()), Math.round(pos1.y()), Math.round(pos1.z()));

        int sky = entity.getLevel().getBrightness(LightLayer.SKY, blockPos);
        int block = entity.getLevel().getBrightness(LightLayer.BLOCK, blockPos);
        int sky1 = entity.getLevel().getBrightness(LightLayer.SKY, blockPos1);
        int block1 = entity.getLevel().getBrightness(LightLayer.BLOCK, blockPos1);

        return LightTexture.pack(Math.max(block, block1), Math.max(sky, sky1));
    }

    private float catenary(double x, double length, int sections) {
        double a = length / ElectricWireItem.MAX_DISTANCE;
        x = (x / sections * 2 - 1);
        return (float) ((Math.cosh(x) - Math.cosh(1.0f)) * a);
    }

    private void wireSection(VertexConsumer consumer, Matrix4f pose, Vector3f from, Vector3f to, int[] color, int light) {
        Vector3f direction = new Vector3f(to).sub(from).normalize();
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

        if (isVertical(direction, up))
            up = new Vector3f(1.0f, 0.0f, 0.0f);

        pose = new Matrix4f(pose)
                .translate(from)
                .rotateTowards(direction, up);

        int r = color[0];
        int g = color[1];
        int b = color[2];
        int z = color[3];

        float f = WIRE_THICKNESS / 2;
        float distance = from.distance(to);

        consumer.vertex(pose, -f, -f, 0.0f).color(r, g, b, z).uv2(light).endVertex();
        consumer.vertex(pose, -f, -f, distance).color(r, g, b, z).uv2(light).endVertex();
        consumer.vertex(pose, f, f, distance).color(r, g, b, z).uv2(light).endVertex();
        consumer.vertex(pose, f, f, 0.0f).color(r, g, b, z).uv2(light).endVertex();

        consumer.vertex(pose, f, -f, 0.0f).color(r, g, b, z).uv2(light).endVertex();
        consumer.vertex(pose, f, -f, distance).color(r, g, b, z).uv2(light).endVertex();
        consumer.vertex(pose, -f, f, distance).color(r, g, b, z).uv2(light).endVertex();
        consumer.vertex(pose, -f, f, 0.0f).color(r, g, b, z).uv2(light).endVertex();
    }

    private boolean isVertical(Vector3f vec, Vector3f up) {
        return vec.equals(up, 0.001f) || vec.equals(up.mul(-1.0f), 0.001f);
    }
}
