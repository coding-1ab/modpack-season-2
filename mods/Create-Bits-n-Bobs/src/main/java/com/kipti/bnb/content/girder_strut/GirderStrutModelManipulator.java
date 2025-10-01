package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.content.girder_strut.cap.GirderCapAccumulator;
import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import com.kipti.bnb.content.girder_strut.mesh.GirderSegmentMesh;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
final class GirderStrutModelManipulator {

    private static final ResourceLocation STONE_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "block/stone");

    private static GirderSegmentMesh segmentMesh;

    private GirderStrutModelManipulator() {
    }

    static List<BakedQuad> bakeConnection(GirderStrutModelBuilder.GirderConnection connection) {
        if (connection.renderLength() <= GirderGeometry.EPSILON) {
            return List.of();
        }

        GirderSegmentMesh mesh = getSegmentMesh();
        List<GirderMeshQuad> quads = mesh.forLength((float) connection.renderLength());

        Vec3 dir = connection.direction();
        double distHorizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float yRot = distHorizontal == 0 ? 0f : (float) Math.atan2(dir.x, dir.z);
        float xRot = (float) Math.atan2(dir.y, distHorizontal);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(connection.start().x, connection.start().y, connection.start().z);
        poseStack.mulPose(new Quaternionf().rotationY(yRot));
        poseStack.mulPose(new Quaternionf().rotationX(-xRot));
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = new Matrix4f(last.pose());
        Matrix3f normalMatrix = new Matrix3f(last.normal());

        Vector3f planePoint = toVector3f(connection.surfacePlanePoint());
        Vector3f planeNormal = toVector3f(connection.surfaceNormal());
        if (planeNormal.lengthSquared() > GirderGeometry.EPSILON) {
            planeNormal.normalize();
        }

        List<BakedQuad> bakedQuads = new ArrayList<>();
        GirderCapAccumulator capAccumulator = new GirderCapAccumulator(STONE_LOCATION);
        for (GirderMeshQuad quad : quads) {
            quad.transformAndEmit(pose, normalMatrix, planePoint, planeNormal, capAccumulator, bakedQuads);
        }
        capAccumulator.emitCaps(planePoint, planeNormal, bakedQuads);
        return bakedQuads;
    }

    private static GirderSegmentMesh getSegmentMesh() {
        if (segmentMesh == null) {
            BakedModel bakedModel = BnbPartialModels.GIRDER_STRUT_SEGMENT.get();
            List<BakedQuad> bakedQuads = new ArrayList<>();
            RandomSource random = RandomSource.create();
            bakedQuads.addAll(bakedModel.getQuads(
                BnbBlocks.GIRDER_STRUT.get().defaultBlockState(),
                null,
                random,
                ModelData.EMPTY,
                null
            ));
            segmentMesh = new GirderSegmentMesh(bakedQuads);
        }
        return segmentMesh;
    }

    private static Vector3f toVector3f(Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }
}
