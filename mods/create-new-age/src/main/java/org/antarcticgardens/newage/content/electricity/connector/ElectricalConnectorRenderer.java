package org.antarcticgardens.newage.content.electricity.connector;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import org.antarcticgardens.newage.content.electricity.wire.WireType;
import org.joml.Matrix4f;

import java.util.Map;

public class ElectricalConnectorRenderer implements BlockEntityRenderer<ElectricalConnectorBlockEntity> {
    public static final RenderType WIRE = RenderType.create(
            "wire",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.DEBUG_LINE_STRIP,
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
            poseStack.pushPose();

            VertexConsumer consumer = buffer.getBuffer(WIRE);

            BlockPos pos = blockEntity.getBlockPos();

            poseStack.translate(0.5f, 0.5f, 0.5f);

            float x = e.getKey().getX() - pos.getX();
            float y = e.getKey().getY() - pos.getY();
            float z = e.getKey().getZ() - pos.getZ();

            Matrix4f pose = poseStack.last().pose();

            consumer.vertex(pose, 0.0f, 0.0f, 0.0f).color(0, 0, 0, 255).uv2(255).endVertex();
            consumer.vertex(pose, x, y, z).color(0, 0, 0, 255).uv2(255).endVertex();

            poseStack.popPose();
        }
    }
}
