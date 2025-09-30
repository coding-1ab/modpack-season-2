package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
final class GirderStrutModelManipulator {

    private static final float EPSILON = 1.0e-4f;
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_LIGHT = LightTexture.pack(15, 15);

    private static SegmentMesh segmentMesh;

    private GirderStrutModelManipulator() {
    }

    static List<BakedQuad> bakeConnection(GirderStrutModelBuilder.GirderConnection connection) {
        if (connection.renderLength() <= EPSILON) {
            return List.of();
        }

        SegmentMesh mesh = getSegmentMesh();
        List<MeshQuad> quads = mesh.forLength((float) connection.renderLength());

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
        if (planeNormal.lengthSquared() > EPSILON) {
            planeNormal.normalize();
        }

        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MeshQuad quad : quads) {
            quad.transformAndEmit(pose, normalMatrix, planePoint, planeNormal, bakedQuads);
        }
        return bakedQuads;
    }

    private static SegmentMesh getSegmentMesh() {
        if (segmentMesh == null) {
            BakedModel bakedModel = BnbPartialModels.GIRDER_STRUT_SEGMENT.get();
            BlockState state = BnbBlocks.GIRDER_STRUT.get().defaultBlockState();
            RandomSource random = RandomSource.create();
            List<BakedQuad> bakedQuads = new ArrayList<>(bakedModel.getQuads(state, null, random, ModelData.EMPTY, null));
            segmentMesh = new SegmentMesh(bakedQuads);
        }
        return segmentMesh;
    }

    private static Vector3f toVector3f(Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }

    private static float signedDistance(Vector3f point, Vector3f planeNormal, Vector3f planePoint) {
        return new Vector3f(point).sub(planePoint).dot(planeNormal);
    }

    private static Vertex interpolate(Vertex start, Vertex end, float t) {
        Vector3f position = new Vector3f(start.position()).lerp(end.position(), t);
        Vector3f normal = new Vector3f(start.normal()).lerp(end.normal(), t);
        if (normal.lengthSquared() > EPSILON) {
            normal.normalize();
        }
        float u = Mth.lerp(t, start.u(), end.u());
        float v = Mth.lerp(t, start.v(), end.v());
        int color = lerpColor(start.color(), end.color(), t);
        int light = lerpPackedLight(start.light(), end.light(), t);
        return new Vertex(position, normal, u, v, color, light);
    }

    private static int lerpColor(int a, int b, float t) {
        if (a == b) {
            return a;
        }
        int aA = (a >>> 24) & 0xFF;
        int aR = (a >>> 16) & 0xFF;
        int aG = (a >>> 8) & 0xFF;
        int aB = a & 0xFF;
        int bA = (b >>> 24) & 0xFF;
        int bR = (b >>> 16) & 0xFF;
        int bG = (b >>> 8) & 0xFF;
        int bB = b & 0xFF;
        int alpha = (int) Mth.clamp(Mth.lerp(t, aA, bA), 0f, 255f);
        int red = (int) Mth.clamp(Mth.lerp(t, aR, bR), 0f, 255f);
        int green = (int) Mth.clamp(Mth.lerp(t, aG, bG), 0f, 255f);
        int blue = (int) Mth.clamp(Mth.lerp(t, aB, bB), 0f, 255f);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int lerpPackedLight(int a, int b, float t) {
        int blockA = a & 0xFFFF;
        int skyA = (a >>> 16) & 0xFFFF;
        int blockB = b & 0xFFFF;
        int skyB = (b >>> 16) & 0xFFFF;
        int block = (int) Mth.clamp(Mth.lerp(t, blockA, blockB), 0f, 0xFFFF);
        int sky = (int) Mth.clamp(Mth.lerp(t, skyA, skyB), 0f, 0xFFFF);
        return (sky << 16) | block;
    }

    private static class SegmentMesh {

        private final List<MeshQuad> baseQuads;

        SegmentMesh(List<BakedQuad> quads) {
            this.baseQuads = quads.stream()
                .map(MeshQuad::from)
                .filter(Objects::nonNull)
                .toList();
        }

        List<MeshQuad> forLength(float length) {
            int fullSegments = Mth.floor(length + EPSILON);
            float partial = length - fullSegments;

            List<MeshQuad> result = new ArrayList<>(baseQuads.size() * (fullSegments + 1));

            for (int i = 0; i < fullSegments; i++) {
                float offset = i;
                for (MeshQuad quad : baseQuads) {
                    result.add(quad.translate(0f, 0f, offset));
                }
            }

            if (partial > EPSILON) {
                for (MeshQuad quad : baseQuads) {
                    MeshQuad clipped = quad.clipZ(partial);
                    if (clipped != null) {
                        result.add(clipped.translate(0f, 0f, fullSegments));
                    }
                }
            }

            if (result.isEmpty()) {
                for (MeshQuad quad : baseQuads) {
                    MeshQuad fallback = quad.clipZ(Math.max(partial, EPSILON));
                    if (fallback != null) {
                        result.add(fallback);
                    }
                }
            }

            return result;
        }
    }

    private static class MeshQuad {

        private final Vertex[] vertices;
        private final TextureAtlasSprite sprite;
        private final Direction nominalFace;
        private final int tintIndex;
        private final boolean shade;

        private MeshQuad(Vertex[] vertices, TextureAtlasSprite sprite, Direction nominalFace, int tintIndex, boolean shade) {
            this.vertices = vertices;
            this.sprite = sprite;
            this.nominalFace = nominalFace;
            this.tintIndex = tintIndex;
            this.shade = shade;
        }

        static MeshQuad from(BakedQuad quad) {
            int[] data = quad.getVertices();
            int stride = BakedQuadHelper.VERTEX_STRIDE;
            Vertex[] vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                Vec3 pos = BakedQuadHelper.getXYZ(data, i);
                Vec3 normal = BakedQuadHelper.getNormalXYZ(data, i);
                float u = BakedQuadHelper.getU(data, i);
                float v = BakedQuadHelper.getV(data, i);
                int baseIndex = stride * i;
                int color = data.length > baseIndex + BakedQuadHelper.COLOR_OFFSET ? data[baseIndex + BakedQuadHelper.COLOR_OFFSET] : DEFAULT_COLOR;
                int light = data.length > baseIndex + BakedQuadHelper.LIGHT_OFFSET ? data[baseIndex + BakedQuadHelper.LIGHT_OFFSET] : DEFAULT_LIGHT;
                vertices[i] = new Vertex(
                    new Vector3f((float) pos.x, (float) pos.y, (float) pos.z),
                    new Vector3f((float) normal.x, (float) normal.y, (float) normal.z),
                    u,
                    v,
                    color,
                    light
                );
            }
            return new MeshQuad(vertices, quad.getSprite(), quad.getDirection(), quad.getTintIndex(), quad.isShade());
        }

        MeshQuad translate(float dx, float dy, float dz) {
            Vertex[] translated = new Vertex[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                Vertex vertex = vertices[i];
                Vector3f pos = new Vector3f(vertex.position()).add(dx, dy, dz);
                translated[i] = new Vertex(pos, new Vector3f(vertex.normal()), vertex.u(), vertex.v(), vertex.color(), vertex.light());
            }
            return new MeshQuad(translated, sprite, nominalFace, tintIndex, shade);
        }

        MeshQuad clipZ(float maxZ) {
            float minZ = Float.POSITIVE_INFINITY;
            float maxOriginalZ = Float.NEGATIVE_INFINITY;
            for (Vertex vertex : vertices) {
                float z = vertex.position().z;
                minZ = Math.min(minZ, z);
                maxOriginalZ = Math.max(maxOriginalZ, z);
            }
            if (maxZ >= maxOriginalZ - EPSILON) {
                return this;
            }
            if (maxZ <= minZ + EPSILON) {
                float translation = maxZ - maxOriginalZ;
                Vertex[] shifted = new Vertex[vertices.length];
                for (int i = 0; i < vertices.length; i++) {
                    Vertex vertex = vertices[i];
                    Vector3f pos = new Vector3f(vertex.position()).add(0f, 0f, translation);
                    shifted[i] = new Vertex(pos, new Vector3f(vertex.normal()), vertex.u(), vertex.v(), vertex.color(), vertex.light());
                }
                // Move the fully sliced quad back to the cut plane so the end face is preserved.
                return new MeshQuad(shifted, sprite, nominalFace, tintIndex, shade);
            }
            List<Vertex> clipped = new ArrayList<>();

            for (int i = 0; i < vertices.length; i++) {
                Vertex current = vertices[i];
                Vertex next = vertices[(i + 1) % vertices.length];

                boolean currentInside = current.position().z <= maxZ + EPSILON;
                boolean nextInside = next.position().z <= maxZ + EPSILON;

                if (currentInside && nextInside) {
                    clipped.add(next);
                } else if (currentInside && !nextInside) {
                    clipped.add(interpolate(current, next, clampT(current, next, maxZ)));
                } else if (!currentInside && nextInside) {
                    clipped.add(interpolate(current, next, clampT(current, next, maxZ)));
                    clipped.add(next);
                }
            }

            if (clipped.size() < 3) {
                return null;
            }

            return new MeshQuad(clipped.toArray(new Vertex[0]), sprite, nominalFace, tintIndex, shade);
        }

        private float clampT(Vertex current, Vertex next, float maxZ) {
            float delta = next.position().z - current.position().z;
            if (Math.abs(delta) < EPSILON) {
                return 0f;
            }
            return (maxZ - current.position().z) / delta;
        }

        void transformAndEmit(
            Matrix4f pose,
            Matrix3f normalMatrix,
            Vector3f planePoint,
            Vector3f planeNormal,
            List<BakedQuad> consumer
        ) {
            List<Vertex> transformed = new ArrayList<>(vertices.length);
            for (Vertex vertex : vertices) {
                Vector3f position = new Vector3f(vertex.position());
                pose.transformPosition(position);
                Vector3f normal = new Vector3f(vertex.normal());
                normalMatrix.transform(normal);
                if (normal.lengthSquared() > EPSILON) {
                    normal.normalize();
                }
                transformed.add(new Vertex(position, normal, vertex.u(), vertex.v(), vertex.color(), vertex.light()));
            }

            List<Vertex> clipped = clipAgainstPlane(transformed, planePoint, planeNormal);
            if (clipped.size() < 3) {
                return;
            }

            emitAsQuads(clipped, consumer);
        }

        private List<Vertex> clipAgainstPlane(List<Vertex> input, Vector3f planePoint, Vector3f planeNormal) {
            if (planeNormal.lengthSquared() <= EPSILON) {
                return input;
            }

            List<Vertex> result = new ArrayList<>();
            int size = input.size();
            Vertex previousVertex = input.get(size - 1);
            float previousDistance = signedDistance(previousVertex.position(), planeNormal, planePoint);

            for (Vertex currentVertex : input) {
                float currentDistance = signedDistance(currentVertex.position(), planeNormal, planePoint);
                boolean currentInside = currentDistance >= -EPSILON;
                boolean previousInside = previousDistance >= -EPSILON;

                if (currentInside) {
                    if (!previousInside) {
                        float t = previousDistance / (previousDistance - currentDistance);
                        result.add(interpolate(previousVertex, currentVertex, t));
                    }
                    result.add(currentVertex);
                } else if (previousInside) {
                    float t = previousDistance / (previousDistance - currentDistance);
                    result.add(interpolate(previousVertex, currentVertex, t));
                }

                previousVertex = currentVertex;
                previousDistance = currentDistance;
            }

            return result;
        }

        private void emitAsQuads(List<Vertex> vertices, List<BakedQuad> consumer) {
            if (vertices.size() == 4) {
                consumer.add(buildQuad(vertices));
                return;
            }
            if (vertices.size() == 3) {
                consumer.add(buildQuad(Arrays.asList(vertices.get(0), vertices.get(1), vertices.get(2), vertices.get(2))));
                return;
            }

            Vertex anchor = vertices.get(0);
            for (int i = 1; i < vertices.size() - 1; i++) {
                List<Vertex> tri = Arrays.asList(anchor, vertices.get(i), vertices.get(i + 1), vertices.get(i + 1));
                consumer.add(buildQuad(tri));
            }
        }

        private BakedQuad buildQuad(List<Vertex> quadVertices) {
            int stride = BakedQuadHelper.VERTEX_STRIDE;
            int[] vertexData = new int[stride * 4];
            for (int i = 0; i < quadVertices.size(); i++) {
                Vertex vertex = quadVertices.get(i);
                Vec3 pos = new Vec3(vertex.position().x, vertex.position().y, vertex.position().z);
                Vec3 normal = new Vec3(vertex.normal().x, vertex.normal().y, vertex.normal().z);
                BakedQuadHelper.setXYZ(vertexData, i, pos);
                BakedQuadHelper.setNormalXYZ(vertexData, i, normal);
                BakedQuadHelper.setU(vertexData, i, vertex.u());
                BakedQuadHelper.setV(vertexData, i, vertex.v());
                int baseIndex = i * stride;
                vertexData[baseIndex + BakedQuadHelper.COLOR_OFFSET] = vertex.color();
                vertexData[baseIndex + BakedQuadHelper.LIGHT_OFFSET] = vertex.light();
            }

            Vector3f avgNormal = new Vector3f();
            for (Vertex vertex : quadVertices) {
                avgNormal.add(vertex.normal());
            }
            Direction face = nominalFace;
            if (avgNormal.lengthSquared() > EPSILON) {
                avgNormal.normalize();
                face = Direction.getNearest(avgNormal.x, avgNormal.y, avgNormal.z);
            }

            return new BakedQuad(vertexData, tintIndex, face, sprite, shade);
        }
    }

    private record Vertex(Vector3f position, Vector3f normal, float u, float v, int color, int light) {
    }
}
