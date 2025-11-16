package com.kipti.bnb.content.cogwheel_chain.block;

import com.kipti.bnb.content.cogwheel_chain.graph.ChainPathNode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Function;

public class CogwheelChainBlockEntityRenderer extends KineticBlockEntityRenderer<CogwheelChainBlockEntity> {

    public static final ResourceLocation CHAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/block/chain.png");
    public static final int MIP_DISTANCE = 48;

    public CogwheelChainBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CogwheelChainBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        float rotationsPerTick = -be.getChainRotationFactor() * be.getSpeed() / (60 * 20);
        float time = AnimationTickHolder.getRenderTime();

        float offset = rotationsPerTick == 0 ? 0 : (float) (((Math.PI * 2 * rotationsPerTick * time) % 1f) + 1f) % 1f;

        //For now, if controller, render an outliner between each chainNode
        Function<Vector3f, Integer> lighter = be.createGlobalLighter();
        if (be.isController && be.chain != null) {
            for (int i = 0; i < be.chain.getChainPathNodes().size(); i++) {
                ChainPathNode nodeA = be.chain.getChainPathNodes().get(i);
                ChainPathNode nodeB = be.chain.getChainPathNodes().get((i + 1) % be.chain.getChainPathNodes().size());

//                Outliner.getInstance()
//                    .showLine(nodeA, nodeA.getPosition(), nodeB.getPosition())
//                    .colored(0xff00ff00)
//                    .lineWidth(0.2f);
                renderChain(be, ms, buffer, light, overlay, nodeA.getPosition(), nodeB.getPosition(), lighter, offset);
            }
        }
    }

    //Mostly just copied from create's chain renderer

    private void renderChain(CogwheelChainBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay, Vec3 from, Vec3 to, Function<Vector3f, Integer> lighter, float offset) {
        Vec3 diff = to.subtract(from);
        double yaw = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
        double pitch = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.y, diff.multiply(1, 0, 1)
            .length());

        BlockPos tilePos = be.getBlockPos();

        Vec3 startOffset = from.subtract(Vec3.atCenterOf(tilePos));

        ms.pushPose();
        var chain = TransformStack.of(ms);
        chain.center();
        chain.translate(startOffset);
        chain.rotateYDegrees((float) yaw);
        chain.rotateXDegrees(90 - (float) pitch);
        chain.rotateYDegrees(45);
        chain.translate(0, 8 / 16f, 0);
        chain.uncenter();

        int light1 = lighter.apply(new Vector3f((float) from.x, (float) from.y, (float) from.z));
        int light2 = lighter.apply(new Vector3f((float) to.x, (float) to.y, (float) to.z));

        boolean far = Minecraft.getInstance().level == be.getLevel() && !Minecraft.getInstance()
            .getBlockEntityRenderDispatcher().camera.getPosition()
            .closerThan(from.lerp(to, 0.5), MIP_DISTANCE);

        renderChain(ms, buffer, offset, (float) from.distanceTo(to), light1, light2, far);

        ms.popPose();
    }

    public static void renderChain(PoseStack ms, MultiBufferSource buffer, float offset, float length, int light1,
                                   int light2, boolean far) {
        float radius = far ? 1f / 16f : 1.5f / 16f;
        float minV = far ? 0 : offset;
        float maxV = far ? 1 / 16f : length + minV;
        float minU = far ? 3 / 16f : 0;
        float maxU = far ? 4 / 16f : 3 / 16f;

        ms.pushPose();
        ms.translate(0.5D, 0.0D, 0.5D);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(CHAIN_LOCATION));
        renderPart(ms, vc, length, 0.0F, radius, radius, 0.0F, -radius, 0.0F, 0.0F, -radius, minU, maxU, minV, maxV,
            light1, light2, far);

        ms.popPose();
    }

    private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer, float pMaxY, float pX0, float pZ0,
                                   float pX1, float pZ1, float pX2, float pZ2, float pX3, float pZ3, float pMinU, float pMaxU, float pMinV,
                                   float pMaxV, int light1, int light2, boolean far) {
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();

        float uO = far ? 0f : 3 / 16f;
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX0, pZ0, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV, light1,
            light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX3, pZ3, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV, light1,
            light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX1, pZ1, pX2, pZ2, pMinU + uO, pMaxU + uO, pMinV, pMaxV,
            light1, light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX2, pZ2, pX1, pZ1, pMinU + uO, pMaxU + uO, pMinV, pMaxV,
            light1, light2);
    }

    private static void renderQuad(Matrix4f pPose, PoseStack.Pose pNormal, VertexConsumer pConsumer, float pMinY, float pMaxY,
                                   float pMinX, float pMinZ, float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV,
                                   int light1, int light2) {
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMinX, pMinZ, pMaxU, pMinV, light2);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMinX, pMinZ, pMaxU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMaxX, pMaxZ, pMinU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMaxX, pMaxZ, pMinU, pMinV, light2);
    }

    private static void addVertex(Matrix4f pPose, PoseStack.Pose pNormal, VertexConsumer pConsumer, float pY, float pX,
                                  float pZ, float pU, float pV, int light) {
        pConsumer.addVertex(pPose, pX, pY, pZ)
            .setColor(1.0f, 1.0f, 1.0f, 1.0f)
            .setUv(pU, pV)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pNormal, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(CogwheelChainBlockEntity blockEntity) {
        return true;
    }

}
