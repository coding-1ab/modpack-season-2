package com.kipti.bnb.content.girder_strut.cap;

import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import com.kipti.bnb.foundation.ClientEvents;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
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

        ClientEvents.clearDeferredDebugRenderOutlines();
        //TEMP DEBUG: put a triangle on each segment to visualize
        for (CapSegment segment : segments) {
            List<CapVertex> loop = new ArrayList<>();
            loop.add(segment.start().copy());
            loop.add(segment.end().copy());
            Vector3f midPoint = new Vector3f(segment.start().position()).add(segment.end
                .position()).mul(0.5f);
            loop.add(new CapVertex(new Vector3f(midPoint).add(new Vector3f(planeNormal).mul(-0.6f)).add((float) (Math.random() * 0.2f), (float) (Math.random() * 0.2f), (float) (Math.random() * 0.2f)), 0f, 0f,
                GirderGeometry.DEFAULT_COLOR, GirderGeometry.DEFAULT_LIGHT, segment.start().sourceSprite()));
            loop.add(loop.get(2)); // Close the loop\

            ClientEvents.pushNewDeferredDebugRenderOutline(
                Pair.of(
                    new Vec3(segment.start().position().x, segment.start().position().y, segment.start().position().z),
                    new Vec3(segment.end().position().x, segment.end().position().y, segment.end().position().z)
                )
            );

            emitLoop(loop.stream().map(loop::indexOf).toList(), loop, segment.tintIndex(), segment.shade(), new Vector3f(planeNormal).cross(new Vector3f(segment.start.position).sub(segment.end.position).normalize()), point, stoneSprite, consumer);
        }
        segments.clear();

//        // Build unique vertex list and edge list
//        List<CapVertex> uniqueVertices = new ArrayList<>();
//        List<LoopEdge> edges = new ArrayList<>();
//
//        for (CapSegment segment : segments) {
//            int startIndex = indexFor(uniqueVertices, segment.start());
//            int endIndex = indexFor(uniqueVertices, segment.end());
//            if (startIndex == endIndex) {
//                continue;
//            }
//            edges.add(new LoopEdge(startIndex, endIndex, segment.tintIndex(), segment.shade()));
//        }
//
//        // Build closed loops from edges - emit each loop separately
//        int loopCount = 0;
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
//            // Keep walking edges until we can't find the next edge or we close the loop
//            int maxSteps = edges.size() + 1; // Prevent infinite loops
//            int steps = 0;
//            while (steps < maxSteps) {
//                steps++;
//                if (current == loop.get(0)) {
//                    // We've returned to the start - loop is closed
//                    closed = true;
//                    break;
//                }
//
//                LoopEdge nextEdge = findAndUseEdge(edges, current);
//                if (nextEdge == null) {
//                    // Can't close the loop, abandon it
//                    break;
//                }
//
//                int nextVertex = nextEdge.other(current);
//                loop.add(nextVertex);
//                current = nextVertex;
//            }
//
//            if (closed && loop.size() > 2) {
//                // Remove the duplicate closing vertex
//                loop.remove(loop.size() - 1);
//                emitLoop(loop, uniqueVertices, tintIndex, shade, normal, point, stoneSprite, consumer);
//                loopCount++;
//            } else {
//            }
//        }
//
//        segments.clear();
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
     * Compare two positions using a very relaxed tolerance to merge vertices that lie on
     * the same edge of the clipping plane, even if they come from different quads.
     * This is necessary because each quad generates its own clipped vertices independently.
     */
    private static boolean positionsClose(org.joml.Vector3f a, org.joml.Vector3f b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        float dz = a.z - b.z;
        // Use a larger tolerance to merge vertices on the same plane edge
        float tol = 0.01f; // 1 centimeter in block units
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
        if (normalizedPlane.lengthSquared() > GirderGeometry.EPSILON) {
            normalizedPlane.normalize();
        }
        Vector3f faceNormal = new Vector3f(normalizedPlane).negate();

        List<GirderVertex> loopVertices = new ArrayList<>(loopIndices.size());
        for (int index : loopIndices) {
            CapVertex data = vertices.get(index);
            // Project the vertex onto the clipping plane
            Vector3f projectedPosition = new Vector3f(data.position());
            float distance = GirderGeometry.signedDistance(projectedPosition, normalizedPlane, planePoint);
            if (Math.abs(distance) > GirderGeometry.EPSILON) {
                projectedPosition.sub(new Vector3f(normalizedPlane).mul(distance));
            }

            // Use proper UV mapping based on position
            // Create a coordinate system on the plane for UV mapping
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

        // Check winding order and reverse if needed
        Vector3f polygonNormal = GirderGeometry.computePolygonNormal(cleaned);
        if (polygonNormal.lengthSquared() > GirderGeometry.EPSILON && polygonNormal.dot(faceNormal) < 0f) {
            java.util.Collections.reverse(cleaned);
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
