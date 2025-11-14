package com.kipti.bnb.content.cogwheel_chain.graph;

import com.google.common.collect.ImmutableList;
import com.kipti.bnb.CreateBitsnBobs;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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
 * -> Use non tangential tangents to calculate the cw / ccw of the best path on each chainNode
 * 	-> Reject if no valid path found, but ultimatley should not be possible, maybe say
 * 		"Couldn't resolve valid chain path. Please report!"
 *
 * -> Build proper chain path, but some assumptions can be made
 * 	-> The path between axis changes will always lie on the valid one,
 * 		-> Essentially just reuse existing tangent code
 * </pre>
 */
public class CogwheelChainPathfinder {

    public record PathNode(PartialCogwheelChainNode chainNode, int side) {
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
                sb.append(String.format("[%s @ %d] -> ", pn.chainNode.pos(), pn.side));
            }
            sb.append(String.format(" | Distance: %.2f | Intersections: %d",
                distance,
                chainIntersections
            ));
            return sb.toString();
        }
    }

    public static Pair<List<PathNode>, List<ChainPathCogwheelNode>> buildChainPath(PartialCogwheelChain chain) {
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
        for (int i = 1; i < chain.getNodes().size() * 2; i++) {
            PartialCogwheelChainNode nextNode = chain.getNodes().get(i % chain.getNodes().size());
            PartialCogwheelChainNode nextNextNode = chain.getNodes().get((i + 1) % chain.getNodes().size());

            AtomicReference<PartialPathFrontierData> nextLeftPath = new AtomicReference<>(null);
            AtomicReference<PartialPathFrontierData> nextRightPath = new AtomicReference<>(null);

            for (Pair<AtomicReference<PartialPathFrontierData>, Integer> pathChannel : List.of(Pair.of(leftPath, 1), Pair.of(rightPath, -1))) {
                AtomicReference<PartialPathFrontierData> fromPath = pathChannel.getFirst();
                if (fromPath.get() == null) {
                    continue;
                }
                int fromSide = pathChannel.getSecond();

                for (int toSide = 1; toSide >= -1; toSide -= 2) {
                    if (isValidPathStep(prevNode, fromSide, nextNode, toSide)) {
                        Vec3 fromPos = prevNode.center().add(
                            getPathingTangentOnCog(nextNode, prevNode, -fromSide)
                        );
                        Vec3 toPos = nextNode.center().add(
                            getPathingTangentOnCog(prevNode, nextNode, toSide)
                        );
                        ImmutableList<PathNode> traversed = fromPath.get().traversed;
                        int traversedSize = traversed.size();

                        double distance = fromPos.distanceTo(toPos) + (getArcDistanceOnCog(
                            prevNode,
                            fromSide,
                            nextNode,
                            toSide,
                            nextNextNode
                        ));

                        int selfIntersections = nextNextNode == prevNode ? 0 : (traversedSize < 2 ? 0 : getSelfIntersection(
                            traversed.get(traversedSize - 2).chainNode,
                            traversed.get(traversedSize - 2).side,
                            traversed.get(traversedSize - 1).chainNode,
                            traversed.get(traversedSize - 1).side,
                            nextNode,
                            toSide
                        ));

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
//                    else {
////                        Outliner.getInstance().showLine("Invalid" + i + fromSide + "to" + toSide,
////                                getPathingTangentOnCog(nextNode, prevNode, -fromSide).add(prevNode.center()),
////                                getPathingTangentOnCog(prevNode, nextNode, toSide).add(nextNode.center()))
////
////                            .colored(0xFF0000);
////                        isValidPathStep(prevNode, fromSide, nextNode, toSide);
//                    }

                }
            }
            leftPath = nextLeftPath;
            rightPath = nextRightPath;

            if (leftPath == null && rightPath == null) {
                CreateBitsnBobs.LOGGER.warn("Failed to build cogwheel chain path at chainNode index {}", i);
                return null;
            }
            prevNode = nextNode;
        }
        PartialPathFrontierData finalPath = (leftPath.get() != null && rightPath.get() != null)
            ? leftPath.get().compare(rightPath.get())
            : (leftPath.get() != null ? leftPath.get() : rightPath.get());
        if (finalPath == null) return null;
        ArrayList<PathNode> finalTraversed = new ArrayList<>(finalPath.traversed);
        finalTraversed.removeLast();
        for (int i = 0; i < chain.getNodes().size() - 1; i++) {//TODO reduce the amount its "overpathing" to just the 2 extra nodes on each side
            finalTraversed.removeFirst();
        }

        return Pair.of(finalTraversed, getPathCogwheelNodesOfPath(chain, finalTraversed));
    }

    /**
     * Walk through the final path and extract cogwheel positions.
     * Should maybe be replaced with a direct generation but its fine this way.
     */
    private static @NotNull ArrayList<ChainPathCogwheelNode> getPathCogwheelNodesOfPath(PartialCogwheelChain chain, ArrayList<PathNode> finalTraversed) {
        ArrayList<ChainPathCogwheelNode> cogwheelPositions = new ArrayList<>();

        BlockPos lastPos = null;
        BlockPos startPos = chain.getFirstNode().pos();
        for (PathNode pathNode : finalTraversed) {
            PartialCogwheelChainNode chainNode = pathNode.chainNode;
            if (chainNode.pos() == lastPos) continue;
            int side = pathNode.side;
            BlockPos offsetToStart = chainNode.pos().subtract(startPos);
            cogwheelPositions.add(new ChainPathCogwheelNode(side, offsetToStart));
            lastPos = chainNode.pos();
        }
        return cogwheelPositions;
    }

    private static double getArcDistanceOnCog(PartialCogwheelChainNode prevNode, int incomingSide, PartialCogwheelChainNode currentNode, int outgoingSide, PartialCogwheelChainNode nextNode) {
        Vec3 fromTangent = getPathingTangentOnCog(prevNode, currentNode, incomingSide);
        Vec3 toTangent = getPathingTangentOnCog(nextNode, currentNode, -outgoingSide);

        Vec3 incomingDiff = currentNode.center().subtract(prevNode.center());
        if (incomingDiff.normalize().dot(toTangent.subtract(fromTangent)) < 0) {
            return 0;
        }

        double angle = Math.acos(
            Math.max(-1.0, Math.min(1.0,
                fromTangent.normalize().dot(toTangent.normalize())
            ))
        );

        double radius = currentNode.isLarge() ? 1.0f : 0.5f;

        return angle * radius;
    }

    public static int getSelfIntersection(PartialCogwheelChainNode from,
                                          int fromSide,
                                          PartialCogwheelChainNode middle,
                                          int currentSide,
                                          PartialCogwheelChainNode to,
                                          int side) {
        if (from.rotationAxis() != to.rotationAxis() || middle.rotationAxis() != to.rotationAxis()) {
            return 0;
        }

        Vec3 prevFromPathPos = getPathingTangentOnCog(middle, from, -fromSide).add(from.center());
        Vec3 prevToPathPos = getPathingTangentOnCog(from, middle, currentSide).add(middle.center());

        Vec3 nextFromPathPos = getPathingTangentOnCog(to, middle, -currentSide).add(middle.center());
        Vec3 nextToPathPos = getPathingTangentOnCog(middle, to, side).add(to.center());

        return doLinesIntersectOnPlane(
            middle.axis(),
            prevFromPathPos.subtract(middle.center()),
            prevToPathPos.subtract(middle.center()),
            nextFromPathPos.subtract(middle.center()),
            nextToPathPos.subtract(middle.center())
        ) ? 1 : 0;
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
        if (!from.isLarge() || !to.isLarge()) {
            return false;
        }

        Vec3 diff = to.center().subtract(from.center());

        Vec3 sideAxis = to.axis();
        Vec3 upAxis = from.axis();

        Vec3 fromTangentOffset = getPathingTangentOnCog(to, from, -fromSide);
        Vec3 toTangentOffset = getPathingTangentOnCog(from, to, toSide);

        return sideAxis.multiply(diff).dot(fromTangentOffset) == 1 && upAxis.multiply(diff).dot(toTangentOffset) == -1;
    }

    public static Vec3 getPathingTangentOnCog(PartialCogwheelChainNode from, PartialCogwheelChainNode to, int toSide) {
        double toRadius = to.isLarge() ? 1.0f : 0.5f;

        Vec3 differenceTo = to.center().subtract(from.center());

        if (from.rotationAxis() != to.rotationAxis()) {
            differenceTo = from.projectDirToAxisPlane(
                to.projectDirToAxisPlane(differenceTo)
            );
        }

        return to.axis().cross(differenceTo).normalize().scale(toSide * toRadius);
    }

}
