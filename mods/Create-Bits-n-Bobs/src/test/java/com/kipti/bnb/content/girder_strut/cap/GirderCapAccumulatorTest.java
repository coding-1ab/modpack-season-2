package com.kipti.bnb.content.girder_strut.cap;

import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GirderCapAccumulatorTest {

    private static final Vector3f PLANE_POINT = new Vector3f(0f, 0f, 0f);
    private static final Vector3f PLANE_NORMAL = new Vector3f(0f, 0f, 1f);

    @Test
    void singleRectangleProducesLoopWithProjectedVertices() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> segments = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(1f, 0f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 0f, 0f), vertex(1f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 1f, 0f), vertex(0f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(0f, 1f, 0f), vertex(0f, 0f, 0f))
        );
        accumulator.addSegments(null, 0, false, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(1, loops.size(), "expected a single loop for the rectangle");

        GirderCapAccumulator.CapLoop loop = loops.getFirst();
        assertEquals(4, loop.vertices().size(), "loop should contain each corner");

        Set<Vector3f> expected = collectProjectedPositions(segments, PLANE_POINT, PLANE_NORMAL);
        assertVerticesMatch(loop.vertices(), expected);
    }

    @Test
    void duplicateSegmentsProduceIndependentLoops() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> baseSegments = List.of(
            new GirderMeshQuad.Segment(vertex(0.25f, 0.25f, 0f), vertex(0.75f, 0.25f, 0f)),
            new GirderMeshQuad.Segment(vertex(0.75f, 0.25f, 0f), vertex(0.75f, 0.75f, 0f)),
            new GirderMeshQuad.Segment(vertex(0.75f, 0.75f, 0f), vertex(0.25f, 0.75f, 0f)),
            new GirderMeshQuad.Segment(vertex(0.25f, 0.75f, 0f), vertex(0.25f, 0.25f, 0f))
        );
        List<GirderMeshQuad.Segment> segments = new ArrayList<>();
        segments.addAll(baseSegments);
        segments.addAll(baseSegments);
        accumulator.addSegments(null, 1, true, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(1, loops.size(), "duplicated segments with identical attributes collapse to a single loop");

        Set<Vector3f> expected = collectProjectedPositions(baseSegments, PLANE_POINT, PLANE_NORMAL);
        GirderCapAccumulator.CapLoop loop = loops.getFirst();
        assertEquals(4, loop.vertices().size(), "loop should contain the square corners");
        assertVerticesMatch(loop.vertices(), expected);
        assertPositiveArea(loop.vertices());
        assertAllProjectedVerticesCovered(loops, segments, PLANE_POINT, PLANE_NORMAL);
    }

    @Test
    void reversedSegmentsDeduplicateToSingleLoop() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> forward = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(1f, 0f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 0f, 0f), vertex(1f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 1f, 0f), vertex(0f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(0f, 1f, 0f), vertex(0f, 0f, 0f))
        );

        List<GirderMeshQuad.Segment> reversed = new ArrayList<>();
        for (GirderMeshQuad.Segment segment : forward) {
            reversed.add(new GirderMeshQuad.Segment(segment.end(), segment.start()));
        }

        List<GirderMeshQuad.Segment> segments = new ArrayList<>();
        segments.addAll(forward);
        segments.addAll(reversed);

        accumulator.addSegments(null, 0, false, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(1, loops.size(), "equivalent forward and reverse segments should collapse to one loop");

        Set<Vector3f> expected = collectProjectedPositions(forward, PLANE_POINT, PLANE_NORMAL);
        assertVerticesMatch(loops.getFirst().vertices(), expected);
    }

    @Test
    void complexSegmentSetProducesExpectedCoverage() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> segments = List.of(
            segment(0.3750f, 1.0300f, 1.0010f, 0.3750f, 0.6768f, 1.0010f),
            segment(0.3750f, 0.6768f, 1.0010f, 0.6250f, 0.6768f, 1.0010f),
            segment(0.6250f, 0.6768f, 1.0010f, 0.6250f, 1.0300f, 1.0010f),
            segment(0.6250f, 1.0300f, 1.0010f, 0.3750f, 1.0300f, 1.0010f),
            segment(0.6250f, 0.9786f, 1.0010f, 0.6250f, 0.2714f, 1.0010f),
            segment(0.3750f, 0.9786f, 1.0010f, 0.3750f, 0.2714f, 1.0010f),
            segment(0.7500f, 0.09467f, 1.0010f, 0.2500f, 0.09467f, 1.0010f),
            segment(0.7500f, 0.2714f, 1.0010f, 0.2500f, 0.2714f, 1.0010f),
            segment(0.2500f, 0.6768f, 1.0010f, 0.2500f, 0.5884f, 1.0010f),
            segment(0.2500f, 0.5884f, 1.0010f, 0.7500f, 0.5884f, 1.0010f),
            segment(0.7500f, 0.5884f, 1.0010f, 0.7500f, 0.6768f, 1.0010f),
            segment(0.7500f, 0.6768f, 1.0010f, 0.2500f, 0.6768f, 1.0010f),
            segment(0.7500f, 0.2714f, 1.0010f, 0.7500f, 0.09467f, 1.0010f),
            segment(0.2500f, 0.2714f, 1.0010f, 0.2500f, 0.09467f, 1.0010f),
            segment(0.7500f, 0.9786f, 1.0010f, 0.2500f, 0.9786f, 1.0010f),
            segment(0.2500f, 1.0820f, 1.0010f, 0.7500f, 1.0820f, 1.0010f),
            segment(0.7500f, 1.0820f, 1.0010f, 0.7500f, 0.9786f, 1.0010f),
            segment(0.2500f, 0.9786f, 1.0010f, 0.2500f, 1.0820f, 1.0010f)
        );

        assertDoesNotThrow(() -> accumulator.addSegments(null, 2, true, segments));
        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(new Vector3f(0f, 0f, 1.001f), PLANE_NORMAL);

        assertEquals(4, loops.size(), "expected four discrete loops for the girder cap");

        Vector3f planePoint = new Vector3f(0f, 0f, 1.001f);
        Set<Vector3f> expected = collectProjectedPositions(segments, planePoint, PLANE_NORMAL);
        for (GirderCapAccumulator.CapLoop loop : loops) {
            assertFalse(loop.vertices().isEmpty(), "loop should contain vertices");
            assertVerticesMatch(loop.vertices(), expected);
            assertPositiveArea(loop.vertices());
        }
        assertAllProjectedVerticesCovered(loops, segments, planePoint, PLANE_NORMAL);
    }

    @Test
    void traversalSkipsInteriorSeamBranches() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> outer = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(2f, 0f, 0f)),
            new GirderMeshQuad.Segment(vertex(2f, 0f, 0f), vertex(2f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(2f, 1f, 0f), vertex(0f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(0f, 1f, 0f), vertex(0f, 0f, 0f))
        );
        List<GirderMeshQuad.Segment> seam = List.of(
            new GirderMeshQuad.Segment(vertex(2f, 1f, 0f), vertex(2.25f, 0.75f, 0f)),
            new GirderMeshQuad.Segment(vertex(2.25f, 0.75f, 0f), vertex(1.5f, 0.5f, 0f)),
            new GirderMeshQuad.Segment(vertex(1.5f, 0.5f, 0f), vertex(2f, 1f, 0f))
        );

        List<GirderMeshQuad.Segment> segments = new ArrayList<>();
        segments.addAll(outer);
        segments.addAll(seam);

        accumulator.addSegments(null, 3, false, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(2, loops.size(), "expected outer rim and seam triangle");

        Set<Vector3f> expectedOuter = collectProjectedPositions(outer, PLANE_POINT, PLANE_NORMAL);
        GirderCapAccumulator.CapLoop outerLoop = loops
            .stream()
            .filter(loop -> loop.vertices().size() == expectedOuter.size())
            .findFirst()
            .orElseThrow(() -> new AssertionError("outer loop was not produced"));

        assertVerticesMatch(outerLoop.vertices(), expectedOuter);
    }

    @Test
    void projectedVerticesMatchRotatedPlaneRim() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        Vector3f planeNormal = new Vector3f(0.40824828f, 0.81649655f, -0.40824828f);
        planeNormal.normalize();
        Vector3f planePoint = new Vector3f(-0.75f, 0.5f, 1.125f);

        Vector3f uAxis = perpendicularAxis(planeNormal);
        Vector3f vAxis = new Vector3f(planeNormal).cross(uAxis);
        if (vAxis.lengthSquared() > GirderGeometry.EPSILON) {
            vAxis.normalize();
        }

        float halfWidth = 0.6f;
        float halfHeight = 0.35f;

        Vector3f[] corners = new Vector3f[] {
            corner(planePoint, uAxis, vAxis, -halfWidth, -halfHeight),
            corner(planePoint, uAxis, vAxis, halfWidth, -halfHeight),
            corner(planePoint, uAxis, vAxis, halfWidth, halfHeight),
            corner(planePoint, uAxis, vAxis, -halfWidth, halfHeight)
        };

        List<GirderMeshQuad.Segment> segments = new ArrayList<>();
        for (int i = 0; i < corners.length; i++) {
            Vector3f start = corners[i];
            Vector3f end = corners[(i + 1) % corners.length];
            segments.add(new GirderMeshQuad.Segment(vertex(start), vertex(end)));
        }

        accumulator.addSegments(null, 4, true, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(planePoint, planeNormal);
        assertEquals(1, loops.size(), "expected a single rotated loop");

        GirderCapAccumulator.CapLoop loop = loops.getFirst();
        Vector3f normalizedPlane = GirderCapAccumulator.normalizedPlane(planeNormal);
        List<Vector3f> projected = GirderCapAccumulator.projectLoopPositions(loop.vertices(), normalizedPlane, planePoint);
        assertEquals(corners.length, projected.size(), "projected vertex count should match rim corners");

        for (Vector3f projectedPos : projected) {
            boolean matched = false;
            for (Vector3f corner : corners) {
                if (closeEnough(projectedPos, corner)) {
                    matched = true;
                    break;
                }
            }
            assertTrue(matched, "Projected vertex %s not matched to expected rim".formatted(projectedPos));

            float distance = Math.abs(GirderGeometry.signedDistance(projectedPos, normalizedPlane, planePoint));
            assertTrue(distance <= GirderGeometry.EPSILON * 4f, "Projected vertex deviates from plane by " + distance);
        }
    }

    @Test
    void degenerateSegmentsAreIgnoredDuringLoopBuild() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> degenerate = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(0f, 0f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 0f, 0f), vertex(1f, 0f, 0f))
        );

        accumulator.addSegments(null, 5, false, degenerate);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertTrue(loops.isEmpty(), "degenerate segments should not yield loops");
    }

    @Test
    void verticalSegmentsProjectedToSinglePointAreDiscarded() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> segments = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(0f, 0f, 1f)),
            new GirderMeshQuad.Segment(vertex(1f, 0f, 0f), vertex(1f, 0f, 1f))
        );

        accumulator.addSegments(null, 6, true, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertTrue(loops.isEmpty(), "segments that collapse onto the same planar point should be ignored");
    }

    @Test
    void openChainsDoNotEmitLoops() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> chain = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(1f, 0f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 0f, 0f), vertex(2f, 0.5f, 0f))
        );

        accumulator.addSegments(null, 7, false, chain);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertTrue(loops.isEmpty(), "open edge chains should not produce polygons");
    }

    @Test
    void loopsAreGroupedByTintAndShade() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));

        List<GirderMeshQuad.Segment> squareA = squareSegments(0f, 0f, 1f, 1f);
        List<GirderMeshQuad.Segment> squareB = squareSegments(2f, 2f, 3f, 3f);

        accumulator.addSegments(null, 3, false, squareA);
        accumulator.addSegments(null, 4, true, squareB);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(2, loops.size(), "two groups should yield two loops");

        GirderCapAccumulator.CapLoop first = loops
            .stream()
            .filter(loop -> loop.key().tintIndex() == 3)
            .findFirst()
            .orElseThrow(() -> new AssertionError("missing tint=3 loop"));
        assertFalse(first.key().shade(), "expected non-shaded loop");

        GirderCapAccumulator.CapLoop second = loops
            .stream()
            .filter(loop -> loop.key().tintIndex() == 4)
            .findFirst()
            .orElseThrow(() -> new AssertionError("missing tint=4 loop"));
        assertTrue(second.key().shade(), "expected shaded loop");

        assertVerticesMatch(first.vertices(), collectProjectedPositions(squareA, PLANE_POINT, PLANE_NORMAL));
        assertVerticesMatch(second.vertices(), collectProjectedPositions(squareB, PLANE_POINT, PLANE_NORMAL));
    }

    @Test
    void zeroLengthPlaneNormalSkipsLoopGeneration() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        accumulator.addSegments(null, 1, false, squareSegments(0f, 0f, 1f, 1f));

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, new Vector3f());
        assertTrue(loops.isEmpty(), "zero-length plane normals should abort loop construction");
    }

    @Test
    void capVerticesRetainAttributesFromSourceGeometry() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        int color = 0xFF3366AA;
        int light = 0x00F0F0F0;
        GirderVertex v0 = detailedVertex(0f, 0f, 0f, 0.1f, 0.2f, color, light);
        GirderVertex v1 = detailedVertex(1f, 0f, 0f, 0.3f, 0.4f, color, light);
        GirderVertex v2 = detailedVertex(1f, 1f, 0f, 0.5f, 0.6f, color, light);
        GirderVertex v3 = detailedVertex(0f, 1f, 0f, 0.7f, 0.8f, color, light);

        List<GirderMeshQuad.Segment> segments = List.of(
            new GirderMeshQuad.Segment(v0, v1),
            new GirderMeshQuad.Segment(v1, v2),
            new GirderMeshQuad.Segment(v2, v3),
            new GirderMeshQuad.Segment(v3, v0)
        );

        accumulator.addSegments(null, 8, false, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        GirderCapAccumulator.CapLoop loop = loops.getFirst();

        Map<String, GirderCapAccumulator.CapVertex> verticesByKey = new HashMap<>();
        for (GirderCapAccumulator.CapVertex vertex : loop.vertices()) {
            verticesByKey.put(quantize(vertex.position()), vertex);
        }

        assertVertexAttributes(verticesByKey.get(quantize(v0.position())), v0);
        assertVertexAttributes(verticesByKey.get(quantize(v1.position())), v1);
        assertVertexAttributes(verticesByKey.get(quantize(v2.position())), v2);
        assertVertexAttributes(verticesByKey.get(quantize(v3.position())), v3);
    }

    @Test
    void projectingLoopPositionsLeavesOriginalVerticesUntouched() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> segments = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0.125f), vertex(1f, 0f, -0.25f)),
            new GirderMeshQuad.Segment(vertex(1f, 0f, -0.25f), vertex(0.5f, 1f, 0.375f)),
            new GirderMeshQuad.Segment(vertex(0.5f, 1f, 0.375f), vertex(0f, 0f, 0.125f))
        );

        accumulator.addSegments(null, 9, false, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(new Vector3f(0f, 0f, 0.125f), PLANE_NORMAL);
        GirderCapAccumulator.CapLoop loop = loops.getFirst();

        List<Vector3f> before = loop.vertices().stream()
            .map(vertex -> new Vector3f(vertex.position()))
            .collect(Collectors.toList());

        Vector3f normalizedPlane = GirderCapAccumulator.normalizedPlane(PLANE_NORMAL);
        List<Vector3f> projected = GirderCapAccumulator.projectLoopPositions(loop.vertices(), normalizedPlane, new Vector3f(0f, 0f, 0.125f));

        for (int i = 0; i < before.size(); i++) {
            Vector3f original = before.get(i);
            Vector3f current = loop.vertices().get(i).position();
            assertTrue(closeEnough(original, current), "source vertex mutated during projection");
            float distance = Math.abs(GirderGeometry.signedDistance(projected.get(i), normalizedPlane, new Vector3f(0f, 0f, 0.125f)));
            assertTrue(distance <= GirderGeometry.EPSILON * 4f, "projected vertex strayed from plane");
        }
    }

    @Test
    void angleWrapAroundStillProducesContinuousLoop() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> segments = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(1f, -0.001f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, -0.001f, 0f), vertex(1.5f, 0.5f, 0f)),
            new GirderMeshQuad.Segment(vertex(1.5f, 0.5f, 0f), vertex(0.25f, 1.25f, 0f)),
            new GirderMeshQuad.Segment(vertex(0.25f, 1.25f, 0f), vertex(0f, 0f, 0f))
        );

        accumulator.addSegments(null, 10, false, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(1, loops.size(), "expected a single loop despite wrap-around angles");

        Set<Vector3f> expected = collectProjectedPositions(segments, PLANE_POINT, PLANE_NORMAL);
        assertVerticesMatch(loops.getFirst().vertices(), expected);
    }

    private static void assertVerticesMatch(List<GirderCapAccumulator.CapVertex> vertices, Set<Vector3f> expected) {
        assertFalse(vertices.isEmpty(), "no vertices emitted for loop");
        for (GirderCapAccumulator.CapVertex vertex : vertices) {
            boolean match = expected.stream().anyMatch(candidate -> closeEnough(candidate, vertex.position()));
            assertTrue(match, "vertex %s not matched against expected set %s".formatted(vertex.position(), expected));
        }
    }

    private static void assertVertexAttributes(GirderCapAccumulator.CapVertex actual, GirderVertex expected) {
        assertTrue(actual != null, "expected vertex was not emitted: " + expected.position());
        assertTrue(closeEnough(actual.position(), expected.position()), "position mismatch for vertex");
        assertEquals(expected.u(), actual.u(), 1.0e-6f, "u mismatch");
        assertEquals(expected.v(), actual.v(), 1.0e-6f, "v mismatch");
        assertEquals(expected.color(), actual.color(), "color mismatch");
        assertEquals(expected.light(), actual.light(), "light mismatch");
    }

    private static void assertAllProjectedVerticesCovered(
        List<GirderCapAccumulator.CapLoop> loops,
        List<GirderMeshQuad.Segment> segments,
        Vector3f planePoint,
        Vector3f planeNormal
    ) {
        Map<String, Integer> usage = new HashMap<>();
        for (GirderMeshQuad.Segment segment : segments) {
            increment(usage, project(segment.start().position(), planePoint, planeNormal));
            increment(usage, project(segment.end().position(), planePoint, planeNormal));
        }

        List<Vector3f> missing = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : usage.entrySet()) {
            if (entry.getValue() < 2) {
                continue;
            }
            Vector3f position = decode(entry.getKey());
            boolean covered = false;
            for (GirderCapAccumulator.CapLoop loop : loops) {
                covered = loop.vertices().stream().anyMatch(vertex -> closeEnough(position, vertex.position()));
                if (covered) {
                    break;
                }
            }
            if (!covered) {
                missing.add(position);
            }
        }
        assertTrue(missing.isEmpty(), "projected vertices were not covered: " + missing);
    }

    private static void assertPositiveArea(List<GirderCapAccumulator.CapVertex> vertices) {
        float area = 0f;
        int size = vertices.size();
        for (int i = 0; i < size; i++) {
            Vector3f current = vertices.get(i).position();
            Vector3f next = vertices.get((i + 1) % size).position();
            area += (current.x * next.y) - (next.x * current.y);
        }
        assertTrue(Math.abs(area) > GirderGeometry.EPSILON, "loop collapsed to zero-area polygon");
    }

    private static void increment(Map<String, Integer> usage, Vector3f position) {
        usage.merge(quantize(position), 1, Integer::sum);
    }

    private static String quantize(Vector3f position) {
        int x = Math.round(position.x / GirderGeometry.EPSILON);
        int y = Math.round(position.y / GirderGeometry.EPSILON);
        int z = Math.round(position.z / GirderGeometry.EPSILON);
        return x + ":" + y + ":" + z;
    }

    private static Vector3f decode(String key) {
        String[] parts = key.split(":");
        float x = Integer.parseInt(parts[0]) * GirderGeometry.EPSILON;
        float y = Integer.parseInt(parts[1]) * GirderGeometry.EPSILON;
        float z = Integer.parseInt(parts[2]) * GirderGeometry.EPSILON;
        return new Vector3f(x, y, z);
    }

    private static Set<Vector3f> collectProjectedPositions(
        List<GirderMeshQuad.Segment> segments,
        Vector3f planePoint,
        Vector3f planeNormal
    ) {
        Set<Vector3f> projected = new HashSet<>();
        for (GirderMeshQuad.Segment segment : segments) {
            projected.add(project(segment.start().position(), planePoint, planeNormal));
            projected.add(project(segment.end().position(), planePoint, planeNormal));
        }
        return projected;
    }

    private static GirderMeshQuad.Segment segment(
        float sx,
        float sy,
        float sz,
        float ex,
        float ey,
        float ez
    ) {
        return new GirderMeshQuad.Segment(vertex(sx, sy, sz), vertex(ex, ey, ez));
    }

    private static Vector3f project(Vector3f position, Vector3f planePoint, Vector3f planeNormal) {
        Vector3f projected = new Vector3f(position);
        float distance = GirderGeometry.signedDistance(projected, planeNormal, planePoint);
        if (Math.abs(distance) > GirderGeometry.EPSILON) {
            projected.sub(new Vector3f(planeNormal).mul(distance));
        }
        return projected;
    }

    private static GirderVertex vertex(float x, float y, float z) {
        return detailedVertex(x, y, z, 0f, 0f, GirderGeometry.DEFAULT_COLOR, GirderGeometry.DEFAULT_LIGHT);
    }

    private static GirderVertex detailedVertex(float x, float y, float z, float u, float v, int color, int light) {
        return new GirderVertex(
            new Vector3f(x, y, z),
            new Vector3f(0f, 0f, -1f),
            u,
            v,
            color,
            light
        );
    }

    private static GirderVertex vertex(Vector3f pos) {
        return vertex(pos.x, pos.y, pos.z);
    }

    private static boolean closeEnough(Vector3f a, Vector3f b) {
        return new Vector3f(a).sub(b).lengthSquared() <= GirderGeometry.EPSILON * GirderGeometry.EPSILON * 4f;
    }

    private static Vector3f perpendicularAxis(Vector3f normal) {
        Vector3f basis = Math.abs(normal.x) < 0.9f ? new Vector3f(1f, 0f, 0f) : new Vector3f(0f, 1f, 0f);
        Vector3f perpendicular = new Vector3f(normal).cross(basis);
        if (perpendicular.lengthSquared() <= GirderGeometry.EPSILON) {
            perpendicular = new Vector3f(normal).cross(new Vector3f(0f, 0f, 1f));
        }
        if (perpendicular.lengthSquared() > GirderGeometry.EPSILON) {
            perpendicular.normalize();
        }
        return perpendicular;
    }

    private static List<GirderMeshQuad.Segment> squareSegments(float minX, float minY, float maxX, float maxY) {
        return List.of(
            new GirderMeshQuad.Segment(vertex(minX, minY, 0f), vertex(maxX, minY, 0f)),
            new GirderMeshQuad.Segment(vertex(maxX, minY, 0f), vertex(maxX, maxY, 0f)),
            new GirderMeshQuad.Segment(vertex(maxX, maxY, 0f), vertex(minX, maxY, 0f)),
            new GirderMeshQuad.Segment(vertex(minX, maxY, 0f), vertex(minX, minY, 0f))
        );
    }

    private static Vector3f corner(Vector3f origin, Vector3f uAxis, Vector3f vAxis, float u, float v) {
        return new Vector3f(origin)
            .add(new Vector3f(uAxis).mul(u))
            .add(new Vector3f(vAxis).mul(v));
    }
}
