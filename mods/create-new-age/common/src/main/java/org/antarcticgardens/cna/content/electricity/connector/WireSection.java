package org.antarcticgardens.cna.content.electricity.connector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;

public class WireSection {
    private static final Map<String, WireSection> cache = new HashMap<>();

    private final float thickness;
    private final float length;
    private final float endYOffset;
    private final float vOffset;

    public WireSection(float length, float thickness, float endYOffset, float vOffset) {
        this.thickness = thickness;
        this.length = length;
        this.endYOffset = endYOffset;
        this.vOffset = vOffset;
    }

    public void render(VertexConsumer consumer, PoseStack poseStack, int packedLight, float yOffset){
        float ht = thickness / 2;
        for (int i = -1; i <= 1; i += 2) {
            addVertex(consumer, ht, ht * i + yOffset, 0.0f, 0.0f, 0.5f + vOffset, poseStack, packedLight);
            addVertex(consumer, -ht, -ht * i + yOffset, 0.0f, 0.0f, 0.0f + vOffset, poseStack, packedLight);
            addVertex(consumer, -ht, -ht * i + endYOffset + yOffset, length * 1.01f, 1.0f, 0.0f + vOffset, poseStack, packedLight);
            addVertex(consumer, ht, ht * i + endYOffset + yOffset, length * 1.01f, 1.0f, 0.5f + vOffset, poseStack, packedLight);
        }
    }

    private void addVertex(VertexConsumer consumer, float x, float y, float z, float u, float v, PoseStack poseStack, int packedLight) {
        consumer.vertex(poseStack.last().pose(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(u, v)
                .uv2(packedLight)
                .normal(0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    public String name() {
        return thickness + "," + length + "," + endYOffset + "," + vOffset;
    }

    public static WireSection getOrCreate(float length, float thickness, float endYOffset, float vOffset) {
        return cache.computeIfAbsent(thickness + "," + length + "," + endYOffset + "," + vOffset,
                s -> new WireSection(length, thickness, endYOffset, vOffset));
    }
}
