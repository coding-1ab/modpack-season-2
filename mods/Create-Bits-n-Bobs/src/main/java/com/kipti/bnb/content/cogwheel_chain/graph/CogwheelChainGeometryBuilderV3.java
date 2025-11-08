package com.kipti.bnb.content.cogwheel_chain.graph;

import com.google.common.collect.ImmutableList;
import com.kipti.bnb.CreateBitsnBobs;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Chain pathing version 3 cause this a b****
 * tldr: layer based flood fill, but we check for self intersections (in an appox way) which are an extra cost, and actually want to maximize distance (there isn't any loops so this is ok)
 * <p>
 * From tldraw (<a href="https://www.tldraw.com/f/P_q_3vRdCBanYptH-4qck?d=v-1769.-567.3320.1890._WwV4NqrB9v64-H-OpMyB">here</a>) of this:
 * <pre>
 * Steps of chain assembly:
 *
 * -> Put list of cogs together, which should have ANY valid path, this validation is good "enough"
 *
 * -> Use non tangential tangents to calculate the cw / ccw of the best path on each node
 * 	-> Reject if no valid path found, but ultimatley should not be possible, maybe say
 * 		"Couldn't resolve valid chain path. Please report!"
 *
 * -> Build proper chain path, but some assumptions can be made
 * 	-> The path between axis changes will always lie on the valid one,
 * 		-> Essentially just reuse existing tangent code
 * </pre>
 */
public class CogwheelChainGeometryBuilderV3 {

    public record PathNode(PartialCogwheelChainNode node, int side) {
    }

    public record PartialPathFrontierData(
        ImmutableList<PathNode> traversed,
        double distance,
        int chainIntersections
    ) {
        public PartialPathFrontierData compare(PartialPathFrontierData other) {
            if (this.chainIntersections != other.chainIntersections) {
                return this.chainIntersections < other.chainIntersections ? this : other;
            }
            return this.distance > other.distance ? this : other;
        }

        public PartialPathFrontierData extend(PathNode nextNode, double additionalDistance, int additionalSelfIntersections) {
            ArrayList<PathNode> newTraversed = new ArrayList<>(this.traversed);
            newTraversed.add(nextNode);
            return new PartialPathFrontierData(
                ImmutableList.copyOf(newTraversed),
                this.distance + additionalDistance,
                this.chainIntersections + additionalSelfIntersections
            );
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Path: ");
            for (PathNode pn : traversed) {
                sb.append(String.format("[%s @ %d] -> ", pn.node.pos(), pn.side));
            }
            sb.append(String.format(" | Distance: %.2f | Intersections: %d",
                distance,
                chainIntersections
            ));
            return sb.toString();
        }
    }

    public static List<PathNode> buildChainPath(PartialCogwheelChain chain) {
        AtomicReference<PartialPathFrontierData> leftPath = new AtomicReference<>(new PartialPathFrontierData(
            ImmutableList.of(new PathNode(chain.getNodes().getFirst(), 1)),
            0,
            0
        ));
        AtomicReference<PartialPathFrontierData> rightPath = new AtomicReference<>(new PartialPathFrontierData(
            ImmutableList.of(new PathNode(chain.getNodes().getFirst(), -1)),
            0,
            0
        ));

        PartialCogwheelChainNode prevNode = chain.getNodes().get(0);
        for (int i = 1; i < chain.getNodes().size() + 1; i++) {
            PartialCogwheelChainNode nextNode = chain.getNodes().get(i % chain.getNodes().size());
            PartialCogwheelChainNode nextNextNode = chain.getNodes().get((i + 1) % chain.getNodes().size());

            AtomicReference<PartialPathFrontierData> nextLeftPath = new AtomicReference<>(null);
            AtomicReference<PartialPathFrontierData> nextRightPath = new AtomicReference<>(null);
            ;

            for (Pair<AtomicReference<PartialPathFrontierData>, Integer> pathChannel : List.of(Pair.of(leftPath, 1), Pair.of(rightPath, -1))) {
                AtomicReference<PartialPathFrontierData> fromPath = pathChannel.getFirst();
                if (fromPath.get() == null) {
                    continue;
                }
                int fromSide = pathChannel.getSecond();

                for (int toSide = 1; toSide >= -1; toSide -= 2) {
//                for (int toSide = 1; toSide <= -1; toSide -= 2) {
                    if (isValidPathStep(prevNode, fromSide, nextNode, toSide)) {
                        Vec3 tangent = getPathingTangentOnCog(prevNode, nextNode, toSide);
                        double distance = tangent.length();

                        int selfIntersections = nextNextNode == prevNode ? 0 : getSelfIntersection(
                            prevNode,
                            nextNode,
                            nextNextNode,
                            toSide
                        );

                        PartialPathFrontierData extendedPath = fromPath.get().extend(
                            new PathNode(nextNode, toSide),
                            distance,
                            selfIntersections
                        );

                        AtomicReference<PartialPathFrontierData> targetPath = (toSide == 1) ? nextLeftPath : nextRightPath;

                        if (targetPath.get() == null) {
                            targetPath.set(extendedPath);
                        } else {
                            targetPath.set(targetPath.get().compare(extendedPath));
                        }
                    }

                }
            }
            leftPath = nextLeftPath;
            rightPath = nextRightPath;

            if (leftPath == null && rightPath == null) {
                CreateBitsnBobs.LOGGER.warn("Failed to build cogwheel chain path at node index {}", i);
                return List.of();
            }
            prevNode = nextNode;
        }
        PartialPathFrontierData finalPath = (leftPath.get() != null && rightPath.get() != null)
            ? leftPath.get().compare(rightPath.get())
            : (leftPath.get() != null ? leftPath.get() : rightPath.get());
//        System.out.println(finalPath);
        ArrayList<PathNode> finalTraversed = new ArrayList<>(finalPath.traversed);
        finalTraversed.removeFirst(); //Remove duplicate final node
        return finalTraversed;
    }

    public static int getSelfIntersection(PartialCogwheelChainNode from, PartialCogwheelChainNode current, PartialCogwheelChainNode to, int side) {
        if (from.rotationAxis() != to.rotationAxis() || current.rotationAxis() != to.rotationAxis()) {
            return 0;
        }

        Vec3 fromPathTangent = getPathingTangentOnCog(from, current, side);
        Vec3 fromStart = fromPathTangent.add(from.center()).subtract(current.center());
        Vec3 fromEnd = fromPathTangent;

        Vec3 toPathTangent = getPathingTangentOnCog(current, to, side);
        Vec3 toStart = toPathTangent;
        Vec3 toEnd = toPathTangent.add(to.center()).subtract(current.center());

        return doLinesIntersectOnPlane(current.axis(), fromStart, fromEnd, toStart, toEnd) ? 1 : 0;
    }

    private static boolean doLinesIntersectOnPlane(Vec3 axis, Vec3 fromStart, Vec3 fromEndWhichIsAroundCenter, Vec3 toStartWhichIsAroundCenter, Vec3 toEnd) {
        Vec3 fromDir = fromEndWhichIsAroundCenter.subtract(fromStart);
        Vec3 toDir = toEnd.subtract(toStartWhichIsAroundCenter);

        Vec3 normal = axis;

        Vec3 diff = toStartWhichIsAroundCenter.subtract(fromStart);
        double denom = normal.dot(fromDir.cross(toDir));
        if (Math.abs(denom) < 1e-7) {
            return false; // Lines are parallel
        }

        double t = normal.dot(diff.cross(toDir)) / denom;
        double u = normal.dot(diff.cross(fromDir)) / denom;

        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

    /**
     * Check if the combination is valid, predominantly dedicated to axis changes
     */
    public static boolean isValidPathStep(PartialCogwheelChainNode from, int fromSide, PartialCogwheelChainNode to, int toSide) {
        if (from.rotationAxis() == to.rotationAxis()) {
            return true;
        }

        Vec3 incoming = from.projectToAxis(to.projectToAxis(to.center().subtract(from.center()).normalize()));

        Vec3 pathTangentFrom = getPathingTangentOnCog(from, to, fromSide);
        Vec3 pathTangentTo = getPathingTangentOnCog(to, from, toSide);

        if (pathTangentFrom.lengthSqr() < 1e-7 || pathTangentTo.lengthSqr() < 1e-7) {
            CreateBitsnBobs.LOGGER.warn("Recived unpathable tangent request between cogwheels at {} and {}", from.pos(), to.pos());
            return false;
        }

        return pathTangentFrom.cross(pathTangentTo).distanceToSqr(incoming) < 1e-5;
    }

    public static Vec3 getPathingTangentOnCog(PartialCogwheelChainNode from, PartialCogwheelChainNode to, int toSide) {
        double toRadius = to.isLarge() ? 1.0f : 0.5f;

        Vec3 incoming = to.center().subtract(from.center()).normalize();

        if (from.rotationAxis() != to.rotationAxis()) {
            Vec3 projectedTo = to.projectToAxis(from.projectToAxis(incoming));
            if (projectedTo.lengthSqr() < 1e-7) {
                CreateBitsnBobs.LOGGER.warn("Recived unpathable tangent request between cogwheels at {} and {}", from.pos(), to.pos());
                return Vec3.ZERO;
            }
        }

        return to.axis().cross(incoming.scale(toSide * toRadius));
    }

}
