package com.kipti.bnb.content.girder_strut.cap;

import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GirderCapAccumulator {

    private final ResourceLocation stoneLocation;
    private final List<CapSegment> segments = new ArrayList<>();

    public GirderCapAccumulator(ResourceLocation stoneLocation) {
        this.stoneLocation = stoneLocation;
    }

    public void addSegments(TextureAtlasSprite sourceSprite, int tintIndex, boolean shade, List<GirderMeshQuad.Segment> newSegments) {
        for (GirderMeshQuad.Segment segment : newSegments) {
            CapVertex start = new CapVertex(segment.start(), sourceSprite);
            CapVertex end = new CapVertex(segment.end(), sourceSprite);
            if (GirderGeometry.positionsEqual(start.position(), end.position())) {
                continue;
            }
            CapSegment candidate = new CapSegment(start, end, tintIndex, shade);
            segments.add(candidate);
        }
    }

    public void emitCaps(Vector3f planePoint, Vector3f planeNormal, List<BakedQuad> consumer) {
        if (segments.isEmpty()) {
            return;
        }
        Vector3f normal = new Vector3f(planeNormal);
        if (normal.lengthSquared() <= GirderGeometry.EPSILON) {
            return;
        }
        normal.normalize();
        Vector3f point = new Vector3f(planePoint);

        TextureAtlasSprite stoneSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stoneLocation);

        List<List<CapVertex>> quads = new ArrayList<>();

        // Now, build quads by linking edges into loops
        while (!segments.isEmpty()) {
            CapSegment startEdge = segments.getFirst();
            List<CapSegment> quad = new ArrayList<>();
            List<CapVertex> quadVertices = new ArrayList<>();

            quad.add(startEdge);
            quadVertices.add(startEdge.start());
            segments.removeFirst();

            CapSegment walk = startEdge;
            boolean found = true;
            while (found) {
                found = false;
                @Nullable CapSegment toRemove = null;
                for (CapSegment other : segments) {
                    if (verticesEqual(walk.end(), other.start())) {
                        toRemove = other;
                        walk = other;
                        quad.add(other);
                        quadVertices.add(other.start());
                        break;
                    }
                }
                if (toRemove != null) {
                    segments.remove(toRemove);
                    found = true;
                }
            }

            quads.add(quadVertices);
        }

        for (List<CapVertex> quadVertices : quads) {
            if (quadVertices.size() < 3) {
                continue;
            }
            emitLoop(quadVertices.stream().map(quadVertices::indexOf).toList(), quadVertices, quadVertices.get(0).color(), false, normal, point, stoneSprite, consumer);
        }

//        for (CapSegment segment : segments) {
//            int startIndex = indexFor(uniqueVertices, segment.start());
//            int endIndex = indexFor(uniqueVertices, segment.end());
//            if (startIndex == endIndex) {
//                continue;
//            }
//            edges.add(new LoopEdge(startIndex, endIndex, segment.tintIndex(), segment.shade()));
//        }
//
//        while (true) {
//            LoopEdge startEdge = findUnusedEdge(edges);
//            if (startEdge == null) {
//                break;
//            }
//
//            List<Integer> loop = new ArrayList<>();
//            loop.add(startEdge.start());
//            loop.add(startEdge.end());
//            startEdge.markUsed();
//
//            int tintIndex = startEdge.tintIndex();
//            boolean shade = startEdge.shade();
//            int current = startEdge.end();
//            boolean closed = false;
//
//            while (current != loop.get(0)) {
//                LoopEdge nextEdge = findAndUseEdge(edges, current);
//                if (nextEdge == null) {
//                    loop.clear();
//                    break;
//                }
//                int nextVertex = nextEdge.other(current);
//                loop.add(nextVertex);
//                current = nextVertex;
//                if (current == loop.get(0)) {
//                    closed = true;
//                }
//            }
//
//            if (closed && loop.size() > 2) {
//                loop.remove(loop.size() - 1);
//                emitLoop(loop, uniqueVertices, tintIndex, shade, normal, point, stoneSprite, consumer);
//            }
//        }

        segments.clear();
    }

    private boolean verticesEqual(CapVertex a, CapVertex b) {
        return positionsClose(a.position(), b.position());
    }

    private int indexFor(List<CapVertex> vertices, CapVertex vertex) {
        for (int i = 0; i < vertices.size(); i++) {
            if (positionsClose(vertices.get(i).position(), vertex.position())) {
                return i;
            }
        }
        vertices.add(vertex.copy());
        return vertices.size() - 1;
    }

    /**
     * Compare two positions using a slightly larger tolerance than the geometry EPSILON
     * to account for floating point differences between intersection calculations from
     * adjacent quads. This helps join cap segments that should meet but are off by a
     * tiny amount.
     */
    private static boolean positionsClose(org.joml.Vector3f a, org.joml.Vector3f b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        float dz = a.z - b.z;
        float tol = GirderGeometry.EPSILON * 10f; // 1e-3
        return dx * dx + dy * dy + dz * dz <= tol * tol;
    }

    private LoopEdge findUnusedEdge(List<LoopEdge> edges) {
        for (LoopEdge edge : edges) {
            if (!edge.used()) {
                return edge;
            }
        }
        return null;
    }

    private LoopEdge findAndUseEdge(List<LoopEdge> edges, int vertexIndex) {
        for (LoopEdge edge : edges) {
            if (edge.used()) {
                continue;
            }
            if (edge.start() == vertexIndex || edge.end() == vertexIndex) {
                edge.markUsed();
                return edge;
            }
        }
        return null;
    }

    private void emitLoop(
        List<Integer> loopIndices,
        List<CapVertex> vertices,
        int tintIndex,
        boolean shade,
        Vector3f planeNormal,
        Vector3f planePoint,
        TextureAtlasSprite stoneSprite,
        List<BakedQuad> consumer
    ) {
        // Use the cut-facing normal (flip the supplied plane normal) so the cap
        // quads face into the cut, not towards the surface.
        Vector3f normalizedPlane = new Vector3f(planeNormal);
        Vector3f faceNormal = new Vector3f(normalizedPlane).negate();
        float planeConstant = normalizedPlane.dot(planePoint);

        List<GirderVertex> loopVertices = new ArrayList<>(loopIndices.size());
        for (int index : loopIndices) {
            CapVertex data = vertices.get(index);
            Vector3f projectedPosition = new Vector3f(data.position());
            float deviation = normalizedPlane.dot(projectedPosition) - planeConstant;
            if (Math.abs(deviation) > GirderGeometry.EPSILON) {
                projectedPosition.fma(-deviation, normalizedPlane);
            }
            float remappedU = GirderGeometry.remapU(data.u(), data.sourceSprite(), stoneSprite);
            float remappedV = GirderGeometry.remapV(data.v(), data.sourceSprite(), stoneSprite);
            loopVertices.add(new GirderVertex(
                projectedPosition,
                new Vector3f(faceNormal),
                remappedU,
                remappedV,
                data.color(),
                data.light()
            ));
        }

        List<GirderVertex> cleaned = GirderGeometry.dedupeLoopVertices(loopVertices);
        if (cleaned.size() < 3) {
            return;
        }

        Vector3f polygonNormal = GirderGeometry.computePolygonNormal(cleaned);
        if (polygonNormal.lengthSquared() > GirderGeometry.EPSILON && polygonNormal.dot(faceNormal) < 0f) {
            Collections.reverse(cleaned);
        }

        Direction face = Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z);
        GirderGeometry.emitPolygon(cleaned, stoneSprite, face, tintIndex, shade, consumer);
    }

    private record CapSegment(CapVertex start, CapVertex end, int tintIndex, boolean shade) {
    }

    private static final class CapVertex {

        private final Vector3f position;
        private final float u;
        private final float v;
        private final int color;
        private final int light;
        private final TextureAtlasSprite sourceSprite;

        CapVertex(GirderVertex vertex, TextureAtlasSprite sprite) {
            this(new Vector3f(vertex.position()), vertex.u(), vertex.v(), vertex.color(), vertex.light(), sprite);
        }

        private CapVertex(Vector3f position, float u, float v, int color, int light, TextureAtlasSprite sourceSprite) {
            this.position = position;
            this.u = u;
            this.v = v;
            this.color = color;
            this.light = light;
            this.sourceSprite = sourceSprite;
        }

        Vector3f position() {
            return position;
        }

        float u() {
            return u;
        }

        float v() {
            return v;
        }

        int color() {
            return color;
        }

        int light() {
            return light;
        }

        TextureAtlasSprite sourceSprite() {
            return sourceSprite;
        }

        CapVertex copy() {
            return new CapVertex(new Vector3f(position), u, v, color, light, sourceSprite);
        }
    }

    private static final class LoopEdge {

        private final int start;
        private final int end;
        private final int tintIndex;
        private final boolean shade;
        private boolean used;

        LoopEdge(int start, int end, int tintIndex, boolean shade) {
            this.start = start;
            this.end = end;
            this.tintIndex = tintIndex;
            this.shade = shade;
        }

        int start() {
            return start;
        }

        int end() {
            return end;
        }

        int tintIndex() {
            return tintIndex;
        }

        boolean shade() {
            return shade;
        }

        boolean used() {
            return used;
        }

        void markUsed() {
            used = true;
        }

        int other(int vertex) {
            return vertex == start ? end : start;
        }
    }
}
