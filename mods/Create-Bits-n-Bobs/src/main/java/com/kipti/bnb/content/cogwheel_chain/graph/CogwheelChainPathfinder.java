package com.kipti.bnb.content.cogwheel_chain.graph;

import com.google.common.collect.ImmutableList;
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

    public record PartialPathFrontierData(
            ImmutableList<PathedCogwheelNode> traversed,
            double distance,
            int chainIntersections
    ) {
        public PartialPathFrontierData compare(PartialPathFrontierData other) {
            if (this.chainIntersections != other.chainIntersections) {
                return this.chainIntersections < other.chainIntersections ? this : other;
            }
            return this.distance > other.distance ? this : other;
        }

        public PartialPathFrontierData extend(PathedCogwheelNode nextNode, double additionalDistance, int additionalSelfIntersections) {
            ArrayList<PathedCogwheelNode> newTraversed = new ArrayList<>(this.traversed);
            newTraversed.add(nextNode);
            return new PartialPathFrontierData(
                    ImmutableList.copyOf(newTraversed),
                    this.distance + additionalDistance,
                    this.chainIntersections + additionalSelfIntersections
            );
        }

    }

    public static List<PathedCogwheelNode> buildChainPath(PlacingCogwheelChain worldSpaceChain) throws CogwheelChain.InvalidGeometryException {
        //Reconstruct chain to local space
        final PlacingCogwheelChain chain = worldSpaceChain.toLocalSpaceChain();

        AtomicReference<PartialPathFrontierData> leftPath = new AtomicReference<>(new PartialPathFrontierData(
                ImmutableList.of(new PathedCogwheelNode(chain.getNodes().getFirst(), 1)),
                0,
                0
        ));
        AtomicReference<PartialPathFrontierData> rightPath = new AtomicReference<>(new PartialPathFrontierData(
                ImmutableList.of(new PathedCogwheelNode(chain.getNodes().getFirst(), -1)),
                0,
                0
        ));

        PlacingCogwheelNode prevNode = chain.getNodes().get(0);
        for (int i = 1; i < chain.getNodes().size() * 2; i++) {
            PlacingCogwheelNode nextNode = chain.getNodes().get(i % chain.getNodes().size());
            PlacingCogwheelNode nextNextNode = chain.getNodes().get((i + 1) % chain.getNodes().size());

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
                        stepPathfinding(prevNode, nextNode, fromSide, toSide, fromPath, nextNextNode, nextLeftPath, nextRightPath, chain.getSize());
                    }
                }
            }
            leftPath = nextLeftPath;
            rightPath = nextRightPath;

            if (leftPath.get() == null && rightPath.get() == null) {
                throw new CogwheelChain.InvalidGeometryException("missing_path_between_nodes");
            }
            prevNode = nextNode;
        }
        PartialPathFrontierData finalPath = (leftPath.get() != null && rightPath.get() != null)
                ? leftPath.get().compare(rightPath.get())
                : (leftPath.get() != null ? leftPath.get() : rightPath.get());
        if (finalPath == null) return null;

//        if (finalPath.chainIntersections > 0) {
//            throw new CogwheelChain.InvalidGeometryException("self_intersections_forbidden");
//        }

        ArrayList<PathedCogwheelNode> finalTraversed = new ArrayList<>(finalPath.traversed);

        finalTraversed.removeLast();
        for (int i = 0; i < chain.getNodes().size() - 1; i++) {//TODO reduce the amount its "overpathing" to just the 2 extra nodes on each sideFactor
            finalTraversed.removeFirst();
        }

        return finalTraversed;
    }

    private static void stepPathfinding(PlacingCogwheelNode prevNode, PlacingCogwheelNode nextNode, int fromSide, int toSide, AtomicReference<PartialPathFrontierData> fromPath, PlacingCogwheelNode nextNextNode, AtomicReference<PartialPathFrontierData> nextLeftPath, AtomicReference<PartialPathFrontierData> nextRightPath, int size) {
        Vec3 fromPos = prevNode.center().add(
                getPathingTangentOnCog(nextNode, prevNode, -fromSide)
        );
        Vec3 toPos = nextNode.center().add(
                getPathingTangentOnCog(prevNode, nextNode, toSide)
        );
        ImmutableList<PathedCogwheelNode> traversed = fromPath.get().traversed;
        int traversedSize = traversed.size();

        double distance = fromPos.distanceTo(toPos) + (getArcDistanceOnCog(
                prevNode,
                nextNode,
                toSide,
                nextNextNode
        ));

        int selfIntersections = nextNextNode == prevNode ? (toSide != fromSide ? 1 : 0) : (traversedSize < 2 ? 0 :
                getSelfIntersection(
                        traversed.get(traversedSize - 2),
                        traversed.get(traversedSize - 1),
                        nextNode,
                        toSide
                ));

        PartialPathFrontierData extendedPath = fromPath.get().extend(
                new PathedCogwheelNode(nextNode, toSide),
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

    private static double getArcDistanceOnCog(PlacingCogwheelNode prevNode, PlacingCogwheelNode currentNode, int outgoingSide, PlacingCogwheelNode nextNode) {
        Vec3 fromTangent = getPathingTangentOnCog(prevNode, currentNode, outgoingSide);
        Vec3 toTangent = getPathingTangentOnCog(nextNode, currentNode, -outgoingSide);

        Vec3 incomingDiff = currentNode.center().subtract(prevNode.center());
        if (toTangent.distanceToSqr(fromTangent) < 1e-4) {
            return 0;
        }

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

    public static int getSelfIntersection(PathedCogwheelNode from,
                                          PathedCogwheelNode middle,
                                          PlacingCogwheelNode to,
                                          int side) {
        if (from.rotationAxis() != to.rotationAxis() || middle.rotationAxis() != to.rotationAxis()) {
            return 0;
        }

        Vec3 prevFromPathPos = getPathingTangentOnCog(middle, from, -from.side()).add(from.center());
        Vec3 prevToPathPos = getPathingTangentOnCog(from, middle, middle.side()).add(middle.center());

        Vec3 nextFromPathPos = getPathingTangentOnCog(to, middle, -middle.side()).add(middle.center());
        Vec3 nextToPathPos = getPathingTangentOnCog(middle, to, side).add(to.center());

        return doLinesIntersectOnPlane(
                middle.rotationAxisVec(),
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
    public static boolean isValidPathStep(PlacingCogwheelNode from, int fromSide, PlacingCogwheelNode to, int toSide) {
        if (from.rotationAxis() == to.rotationAxis()) {
            return true;
        }
        if (!from.isLarge() || !to.isLarge()) {
            return false;
        }

        final Vec3 diff = to.center().subtract(from.center());

        final Vec3 sideAxis = to.rotationAxisVec();
        final Vec3 upAxis = from.rotationAxisVec();

        final Vec3 fromTangentOffset = getPathingTangentOnCog(to, from, -fromSide);
        final Vec3 toTangentOffset = getPathingTangentOnCog(from, to, toSide);

        return sideAxis.multiply(diff).dot(fromTangentOffset) == 1 && upAxis.multiply(diff).dot(toTangentOffset) == -1;
    }

    public static Vec3 getPathingTangentOnCog(final PathedCogwheelNode from, final PathedCogwheelNode to, final int toSide) {
        return getPathingTangentOnCog(from.center(), from.rotationAxisVec(), to.center(), to.isLarge(), to.rotationAxisVec(), toSide);
    }

    public static Vec3 getPathingTangentOnCog(final PathedCogwheelNode from, final PlacingCogwheelNode to, final int toSide) {
        return getPathingTangentOnCog(from.center(), from.rotationAxisVec(), to.center(), to.isLarge(), to.rotationAxisVec(), toSide);
    }

    public static Vec3 getPathingTangentOnCog(final PlacingCogwheelNode from, final PathedCogwheelNode to, final int toSide) {
        return getPathingTangentOnCog(from.center(), from.rotationAxisVec(), to.center(), to.isLarge(), to.rotationAxisVec(), toSide);
    }

    public static Vec3 getPathingTangentOnCog(final PlacingCogwheelNode from, final PlacingCogwheelNode to, final int toSide) {
        return getPathingTangentOnCog(from.center(), from.rotationAxisVec(), to.center(), to.isLarge(), to.rotationAxisVec(), toSide);
    }

    public static Vec3 getPathingTangentOnCog(final Vec3 fromCenter, final Vec3 fromRotationAxis, final Vec3 toCenter, final boolean toLarge, final Vec3 toRotationAxis, final int toSide) {
        final double toRadius = toLarge ? 1.0f : 0.5f;

        Vec3 differenceTo = toCenter.subtract(fromCenter);

        if (!fromRotationAxis.equals(toRotationAxis)) {
            differenceTo = projectDirToAxisPlane(projectDirToAxisPlane(differenceTo, toRotationAxis), fromRotationAxis);
        }

        return toRotationAxis.cross(differenceTo).normalize().scale(toSide * toRadius);
    }

    public static Vec3 projectDirToAxisPlane(final Vec3 vec, final Vec3 axisVec) {
        return vec.subtract(axisVec.multiply(vec));
    }
}
