package com.kipti.bnb.content.cogwheel_chain.graph;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CogwheelChainGeometryBuilder {

    public static List<RenderedChainPathNode> buildFullChainFromPathNodes(List<PathedCogwheelNode> pathNodes) {
        final List<RenderedChainPathNode> resultNodes = new ArrayList<>();
        final List<Pair<Vec3, Vec3>> offsetsAtNodes = new ArrayList<>();
        final int n = pathNodes.size();
        for (int i = 0; i < n; i++) {
            final PathedCogwheelNode previousNode = pathNodes.get((n + i - 1) % n);
            final PathedCogwheelNode currentNode = pathNodes.get(i);
            final PathedCogwheelNode nextNode = pathNodes.get((i + 1) % n);

            final Pair<Vec3, Vec3> inOutPositionsAtThisNode = calculateOffsets(
                    previousNode,
                    currentNode,
                    nextNode
            );
            offsetsAtNodes.add(inOutPositionsAtThisNode);
        }

        for (int i = 0; i < n; i++) {
            final PathedCogwheelNode previousNode = pathNodes.get((n + i - 1) % n);
            final PathedCogwheelNode currentNode = pathNodes.get(i);
            final PathedCogwheelNode nextNode = pathNodes.get((i + 1) % n);

            final Pair<Vec3, Vec3> previousOffsets = offsetsAtNodes.get((i - 1 + n) % n);
            final Pair<Vec3, Vec3> currentOffsets = offsetsAtNodes.get(i);
            final Pair<Vec3, Vec3> nextOffsets = offsetsAtNodes.get((i + 1) % n);

            resultNodes.add(new RenderedChainPathNode(currentNode.localPos(), currentOffsets.getFirst()));
            resultNodes.addAll(
                    wrappedArcBetweenPoints(
                            currentNode,
                            previousOffsets.getSecond().add(previousNode.center()),
                            currentOffsets.getFirst().add(currentNode.center()),
                            currentOffsets.getSecond().add(currentNode.center()),
                            nextOffsets.getFirst().add(nextNode.center())
                    )
            );
            resultNodes.add(new RenderedChainPathNode(currentNode.localPos(), currentOffsets.getSecond()));
        }

        return resultNodes;
    }

    private static List<RenderedChainPathNode> wrappedArcBetweenPoints(
            PathedCogwheelNode currentNode,
            Vec3 outPreviousPositionWorld, Vec3 inCurrentOffsetWorld,
            Vec3 outCurrentOffsetWorld, Vec3 inNextPositionWorld) {
        // Move to chainNode-local frame (center = origin) for rotation math
        Vec3 center = currentNode.localPos().getCenter();

        Vec3 prevLocal = outPreviousPositionWorld.subtract(center);
        Vec3 startLocal = inCurrentOffsetWorld.subtract(center);
        Vec3 endLocal = outCurrentOffsetWorld.subtract(center);
        Vec3 nextLocal = inNextPositionWorld.subtract(center);

        // Basic checks
        double r = startLocal.length();
        if (r <= 1e-9) {
            // degenerate radius zero — nothing to do
            return List.of();
        }

        Vec3 inDirection = startLocal.subtract(prevLocal);
        if (inDirection.length() < 1e-9) inDirection = nextLocal.subtract(startLocal); // fallback
        inDirection = inDirection.normalize();

        Vec3 outDirection = endLocal.subtract(nextLocal);
        if (outDirection.length() < 1e-9) outDirection = prevLocal.subtract(endLocal);
        outDirection = outDirection.normalize();

        Vec3 axis = getDirectionOfAxis(currentNode).normalize();
        if (axis == null) throw new IllegalStateException("axis null for chainNode " + currentNode);

        Vec3 u = startLocal.normalize();
        Vec3 w = endLocal.normalize();

        // signed minimal angle from u -> w around axis in range (-PI, PI]
        double crossDot = axis.dot(u.cross(w));
        double dot = Math.max(-1.0, Math.min(1.0, u.dot(w)));
        double signedAngle = Math.atan2(crossDot, dot); // minimal signed

        // tangent for positive increase at start: dP/dθ = axis x start
        Vec3 tangentAtStart = axis.cross(startLocal);
        double tangentLen = tangentAtStart.length();
        double tangentDot = 0.0;
        if (tangentLen > 1e-9) {
            tangentAtStart = tangentAtStart.scale(1.0 / tangentLen);
            tangentDot = inDirection.dot(tangentAtStart);
        } else {
            // degenerate tangent (start radial zero?) - fallback
            tangentDot = inDirection.dot(u) > 0 ? 1.0 : -1.0;
        }

        // If the incoming direction indicates the chain moves opposite to the signedAngle,
        // prefer the other arc (i.e., add/subtract 2π) so motion along the arc aligns with inDirection.
        final double EPS = 1e-9;
        if (Math.abs(tangentDot) > EPS) {
            double angleSign = Math.signum(signedAngle);
            double desiredSign = (tangentDot > 0) ? 1.0 : -1.0;

            if (angleSign == 0.0) {
                // start and end nearly colinear: either same point or opposite
                if (dot > 0.999999) {
                    signedAngle = 0.0; // identical: no arc
                } else {
                    // opposite: choose pi or -pi based on desired tangent
                    signedAngle = desiredSign * Math.PI;
                }
            } else if (angleSign != desiredSign) {
                // choose the long way so the tangent direction matches
                signedAngle = signedAngle - Math.signum(signedAngle) * 2.0 * Math.PI;
            }
        }

        // Choose segments based on arc length for consistent spacing
        double absAngle = Math.abs(signedAngle);
        double approxArcLength = r * absAngle;
        int segments = Math.max(1, (int) (Math.ceil(approxArcLength) * 2f));
        List<RenderedChainPathNode> result = new ArrayList<>();

        // generate interior points (exclude endpoints)
        for (int i = 1; i < segments; i++) {
            double t = (double) i / (double) segments;
            double theta = signedAngle * t;
            Vec3 rotatedLocal = rotateAroundAxis(startLocal, axis, theta);
            result.add(new RenderedChainPathNode(currentNode.localPos(), rotatedLocal));
        }

        return result;
    }

    private static Vec3 rotateAroundAxis(Vec3 v, Vec3 axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return v.scale(cos)
                .add(axis.cross(v).scale(sin))
                .add(axis.scale(axis.dot(v) * (1 - cos)));
    }

    private static int getLooped(int[] concavities, int i) {
        return concavities[(i + concavities.length) % concavities.length];
    }

    public static Pair<Vec3, Vec3> calculateOffsets(PathedCogwheelNode previousNode,
                                                    PathedCogwheelNode currentNode,
                                                    PathedCogwheelNode nextNode) {

//        Vec3 incomingDirection = getConnectionDirection(previousNode, currentNode);
//        Vec3 outgoingDirection = getConnectionDirection(currentNode, nextNode);

        Vec3 incomingPointOnCircle = getTangentPointOnCircle(previousNode, currentNode, true);
        Vec3 outgoingPointOnCircle = getTangentPointOnCircle(nextNode, currentNode, false);

        return Pair.of(incomingPointOnCircle, outgoingPointOnCircle);
    }

    private static @NotNull Vec3 getDirectionOfAxis(final PathedCogwheelNode currentNode) {
        return Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(currentNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
    }

    private static @NotNull Vec3 getConnectionDirection(final PathedCogwheelNode previousNode, final PathedCogwheelNode currentNode) {
        Vec3 incomingADiff = currentNode.center().subtract(previousNode.center());

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            final Vec3 previousAxis = getDirectionOfAxis(previousNode);
            incomingADiff = incomingADiff.subtract(previousAxis.scale(incomingADiff.dot(previousAxis)));
        }

        final Vec3 axis = getDirectionOfAxis(currentNode);
        return incomingADiff.subtract(axis.scale(axis.dot(incomingADiff)));
    }

    public static Vec3 getTangentPointOnCircle(PathedCogwheelNode previousNode,
                                               PathedCogwheelNode currentNode,
                                               boolean isIncoming) {
        Vec3 axis = getDirectionOfAxis(currentNode);
        Vec3 incoming = isIncoming ? getConnectionDirection(previousNode, currentNode) : getConnectionDirection(currentNode, previousNode);

        double previousRadius = previousNode.isLarge() ? 1.0f : 0.5f;
        double currentRadius = currentNode.isLarge() ? 1.0f : 0.5f;

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            //Find the common line
//            Direction.Axis other = Direction.Axis.values()[Integer.numberOfTrailingZeros(0b111 ^ (1 << currentNode.rotationAxis().ordinal()) ^ (1 << previousNode.rotationAxis().ordinal()))];
//            Vec3 otherAxis = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(other, Direction.AxisDirection.POSITIVE).getNormal());
//            return otherAxis.cross(axis).normalize().scale(
//                currentNode.pos().subtract(previousNode.pos()).get(previousNode.rotationAxis()) * (isIncoming ? 1 : -1)
//            );
            return getDirectionOfAxis(previousNode).scale(previousNode.localPos().subtract(currentNode.localPos()).get(previousNode.rotationAxis()));
        }

        if (previousNode.side() == currentNode.side()) {
            return incoming.normalize().cross(axis).scale(-currentRadius * currentNode.side());
        }

        double factor = previousRadius / (previousRadius + currentRadius);

        Vec3 tangentOrigin = incoming.scale(factor);
        double distance = (isIncoming ? 1 : -1) * tangentOrigin.length();

        double sineRatio = previousRadius / distance;

        double cosRatio = Math.sqrt(1 - sineRatio * sineRatio);

        //Now to find the tangents positon
        double perpendicularHeight = cosRatio * currentRadius;

        double lengthAlongIncoming = sineRatio * currentRadius;

        return incoming.normalize().cross(axis).scale(-perpendicularHeight * currentNode.side())
                .add(incoming.normalize().scale(-lengthAlongIncoming));
    }

}
