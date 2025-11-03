package com.kipti.bnb.content.cogwheel_chain.graph;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CogwheelChainGeometryBuilder {

    public static List<CogwheelChainNode> buildFullChainFromPartial(PartialCogwheelChain source) {
        List<PartialCogwheelChainNode> sourceNodes = source.getNodes();
        List<CogwheelChainNode> resultNodes = new ArrayList<>();

        int[] concavities = new int[sourceNodes.size()];
        for (int i = 0; i < sourceNodes.size(); i++) {
            PartialCogwheelChainNode previousNode = source.getNodeLooped(i - 1);
            PartialCogwheelChainNode currentNode = sourceNodes.get(i);
            PartialCogwheelChainNode nextNode = source.getNodeLooped(i + 1);
            concavities[i] = getNodeConcavity(previousNode, currentNode, nextNode);
        }

        List<Pair<Vec3, Vec3>> offsetsAtNodes = new ArrayList<>();
        for (int i = 0; i < sourceNodes.size(); i++) {
            PartialCogwheelChainNode previousNode = source.getNodeLooped(i - 1);
            PartialCogwheelChainNode currentNode = sourceNodes.get(i);
            PartialCogwheelChainNode nextNode = source.getNodeLooped(i + 1);

            Pair<Vec3, Vec3> inOutPositionsAtThisNode = calculateOffsets(
                previousNode,
                getLooped(concavities, i - 1),
                currentNode,
                concavities[i],
                nextNode,
                getLooped(concavities, i + 1)
            );
            offsetsAtNodes.add(inOutPositionsAtThisNode);
        }

        for (int i = 0; i < sourceNodes.size(); i++) {
            PartialCogwheelChainNode previousNode = source.getNodeLooped(i - 1);
            PartialCogwheelChainNode currentNode = sourceNodes.get(i);
            PartialCogwheelChainNode nextNode = source.getNodeLooped(i + 1);

            int n = sourceNodes.size();
            Pair<Vec3, Vec3> previousOffsets = offsetsAtNodes.get((i - 1 + n) % n);
            Pair<Vec3, Vec3> currentOffsets = offsetsAtNodes.get(i);
            Pair<Vec3, Vec3> nextOffsets = offsetsAtNodes.get((i + 1) % n);

            resultNodes.add(new CogwheelChainNode(currentNode.pos(), currentOffsets.getFirst()));
            resultNodes.addAll(
                wrappedArcBetweenPoints(
                    currentNode,
                    previousOffsets.getSecond().add(previousNode.pos().getCenter()),
                    currentOffsets.getFirst().add(currentNode.pos().getCenter()),
                    currentOffsets.getSecond().add(currentNode.pos().getCenter()),
                    nextOffsets.getFirst().add(nextNode.pos().getCenter())
                )
            );
            resultNodes.add(new CogwheelChainNode(currentNode.pos(), currentOffsets.getSecond()));
        }

        return resultNodes;
    }

    private static List<CogwheelChainNode> wrappedArcBetweenPoints(
        PartialCogwheelChainNode currentNode,
        Vec3 outPreviousPositionWorld, Vec3 inCurrentOffsetWorld,
        Vec3 outCurrentOffsetWorld, Vec3 inNextPositionWorld) {

//        Vec3 inDirection = outPreviousPosition.subtract(inCurrentPosition).normalize();
//        Vec3 outDirection = inNextPosition.subtract(outCurrentPosition).normalize();
//        Vec3 axis = getDirectionOfAxis(currentNode).normalize();
//
//        Vec3 start = inCurrentPosition;
//        Vec3 end = outCurrentPosition;
//        double radius = start.length();
//
//        Vec3 u = start.normalize();
//        Vec3 w = end.normalize();
//
//        // Clamp dot product to [-1, 1] to avoid NaN due to floating error
//        double dot = Math.max(-1.0, Math.min(1.0, u.dot(w)));
//        double angle = Math.acos(dot);
//
//        // Determine rotation direction using triple product (robust)
//        double dirSign = Math.signum(axis.dot(u.cross(w)));
//        angle *= dirSign; // positive means CCW around axis (right-hand rule)
//
//        // Now check incoming direction alignment — flip if opposite of tangent
//        Vec3 tangentAtStart = axis.cross(start).normalize();
//        if (inDirection.dot(tangentAtStart) < 0) {
//            angle = -angle;
//        }
//
//        int segments = 16;
//        List<CogwheelChainNode> result = new ArrayList<>();
//
//        for (int i = 1; i < segments; i++) {
//            double t = (double) i / segments;
//            double theta = angle * t;
//
//            Vec3 rotated = rotateAroundAxis(start, axis, theta);
//            result.add(new CogwheelChainNode(currentNode.pos(), rotated));
//        }
//
//        return result;

        // Move to node-local frame (center = origin) for rotation math
        Vec3 center = currentNode.pos().getCenter();

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
        if (axis == null) throw new IllegalStateException("axis null for node " + currentNode);

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

        // Optionally: ensure axis orientation consistency with neighbour (uncomment if you track prevAxis)
        // Vec3 prevAxis = ...; if (prevAxis != null && prevAxis.dot(axis) < 0) axis = axis.scale(-1);

        // Choose segments based on arc length for consistent spacing
        double absAngle = Math.abs(signedAngle);
        double approxArcLength = r * absAngle;
        int segments = Math.max(1, (int) Math.ceil(approxArcLength));
        List<CogwheelChainNode> result = new ArrayList<>();

        // generate interior points (exclude endpoints)
        for (int i = 1; i < segments; i++) {
            double t = (double) i / (double) segments;
            double theta = signedAngle * t;
            Vec3 rotatedLocal = rotateAroundAxis(startLocal, axis, theta);
            result.add(new CogwheelChainNode(currentNode.pos(), rotatedLocal));
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

    public static Pair<Vec3, Vec3> calculateOffsets(PartialCogwheelChainNode previousNode,
                                                    int previousConcavity,
                                                    PartialCogwheelChainNode currentNode,
                                                    int currentConcavity,
                                                    PartialCogwheelChainNode nextNode,
                                                    int nextConcavity) {
        Vec3 axis = getDirectionOfAxis(currentNode);

//        Vec3 incomingPointOnCircle = get3DTangentOffset(currentNode, previousNode, currentConcavity, previousConcavity);
//        Vec3 outgoingPointOnCircle = get3DTangentOffset(nextNode, currentNode, nextConcavity, currentConcavity);

        Vec3 incomingDirection = getConnectionDirection(previousNode, currentNode);
        Vec3 outgoingDirection = getConnectionDirection(currentNode, nextNode);

        Vec3 incomingPointOnCircle = getTangentPointOnCircle(axis, getConnectionDirection(previousNode, currentNode), previousNode, previousConcavity, currentNode, currentConcavity, true);
        Vec3 outgoingPointOnCircle = getTangentPointOnCircle(axis, getConnectionDirection(currentNode, nextNode), nextNode, nextConcavity, currentNode, currentConcavity, false);

        //Calculate arc
//        List<Vec3> result = new ArrayList<>();
//        result.add(incomingPointOnCircle);
//
//        double angleBetween = Math.acos(
//            incomingPointOnCircle.normalize().dot(outgoingPointOnCircle.normalize())
//        );

//        if (currentConcavity != previousConcavity && currentConcavity != nextConcavity && currentConcavity == 1 && angleBetween < Math.PI) {
//            angleBetween = (2 * Math.PI) - angleBetween;
//        }

        Outliner.getInstance().showLine(
            currentNode + "_incoming",
            currentNode.pos().getCenter(),
            incomingDirection.normalize().scale(-1).add(currentNode.pos().getCenter())
        ).colored(0xff0022);
        Outliner.getInstance().showLine(
            currentNode + "_outgoing",
            currentNode.pos().getCenter(),
            outgoingDirection.normalize().add(currentNode.pos().getCenter())
        ).colored(0x00ff00);
        Outliner.getInstance().showAABB(
            currentNode + "_concavity",
            new AABB(currentNode.pos())
        ).colored(currentConcavity == -1 ? 0x00ff00 : 0xff0000);

//        double radius = currentNode.isLarge() ? 1.0f : 0.5f;
//        double arcLength = angleBetween * radius;
//        int segments = (int) Math.ceil(arcLength / (6 / 16f));

//        for (int i = 1; i < segments; i++) {
//            double t = (double) i / (double) segments;
//            //Slerp
//            double sinTotal = Math.sin(angleBetween);
//            double sinStart = Math.sin((1 - t) * angleBetween);
//            double sinEnd = Math.sin(t * angleBetween);
//
//            Vec3 pointOnCircle = incomingPointOnCircle.scale(sinStart / sinTotal).add(
//                outgoingPointOnCircle.scale(sinEnd / sinTotal)
//            ).normalize().scale(radius);
//
//            result.add(pointOnCircle);
//        }

//        result.add(outgoingPointOnCircle);
        return Pair.of(incomingPointOnCircle, outgoingPointOnCircle);
    }

    private static @NotNull Vec3 getDirectionOfAxis(PartialCogwheelChainNode currentNode) {
        return Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(currentNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
    }

    private static @NotNull Vec3 getConnectionDirection(PartialCogwheelChainNode previousNode, PartialCogwheelChainNode currentNode) {
        Vec3 incomingADiff = currentNode.pos().getCenter().subtract(previousNode.pos().getCenter());

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            Vec3 previousAxis = getDirectionOfAxis(previousNode);
            incomingADiff = incomingADiff.subtract(previousAxis.scale(incomingADiff.dot(previousAxis)));
        }

        Vec3 axis = getDirectionOfAxis(currentNode);
        return incomingADiff.subtract(axis.scale(axis.dot(incomingADiff)));
    }

    public static Vec3 getTangentPointOnCircle(Vec3 axis,
                                               Vec3 incoming,
                                               PartialCogwheelChainNode previousNode,
                                               int previousConcavity,
                                               PartialCogwheelChainNode currentNode,
                                               int currentConcavity,
                                               boolean isIncoming) {
        double previousRadius = previousNode.isLarge() ? 1.0f : 0.5f;
        double currentRadius = currentNode.isLarge() ? 1.0f : 0.5f;

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            //Find the common line
//            Direction.Axis other = Direction.Axis.values()[Integer.numberOfTrailingZeros(0b111 ^ (1 << currentNode.rotationAxis().ordinal()) ^ (1 << previousNode.rotationAxis().ordinal()))];
//            Vec3 otherAxis = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(other, Direction.AxisDirection.POSITIVE).getNormal());
//            return otherAxis.cross(axis).normalize().scale(
//                currentNode.pos().subtract(previousNode.pos()).get(previousNode.rotationAxis()) * (isIncoming ? 1 : -1)
//            );
            return getDirectionOfAxis(previousNode).scale(previousNode.pos().subtract(currentNode.pos()).get(previousNode.rotationAxis()));
        }

        if (previousConcavity == currentConcavity) {
            return incoming.normalize().cross(axis).scale(-currentRadius * currentConcavity);
        }

        double factor = previousRadius / (previousRadius + currentRadius);

        Vec3 tangentOrigin = incoming.scale(factor);
        double distance = tangentOrigin.length();

        double sineRatio = currentRadius / distance;

        double cosRatio = Math.sqrt(1 - sineRatio * sineRatio);

        //Now to find the tangents positon
        double perpendicularHeight = cosRatio * currentRadius;

        double lengthAlongIncoming = sineRatio * currentRadius;

        return incoming.normalize().cross(axis).scale(-perpendicularHeight * currentConcavity)
            .add(incoming.normalize().scale((isIncoming ? 1 : -1) * -lengthAlongIncoming));
    }

    public static int getNodeConcavity(PartialCogwheelChainNode previousNode, PartialCogwheelChainNode currentNode, PartialCogwheelChainNode nextNode) {
        Vec3 axis = getDirectionOfAxis(currentNode);

        Vec3 incomingDiff = currentNode.pos().getCenter().subtract(previousNode.pos().getCenter());
        Vec3 outgoingDiff = nextNode.pos().getCenter().subtract(currentNode.pos().getCenter());

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            Vec3 previousAxis = getDirectionOfAxis(previousNode);
            incomingDiff = incomingDiff.subtract(previousAxis.scale(incomingDiff.dot(previousAxis)));
        }
        if (nextNode.rotationAxis() != currentNode.rotationAxis()) {
            Vec3 nextAxis = getDirectionOfAxis(nextNode);
            outgoingDiff = outgoingDiff.subtract(nextAxis.scale(outgoingDiff.dot(nextAxis)));
        }

        Vec3 incoming = incomingDiff.normalize();
        Vec3 outgoing = outgoingDiff.normalize();

        incoming = incoming.subtract(axis.scale(incoming.dot(axis))).normalize();
        outgoing = outgoing.subtract(axis.scale(outgoing.dot(axis))).normalize();

        double dot = axis.dot(incoming.cross(outgoing));
        return (dot == 0 ? 1 : (int) -Math.signum(dot));
    }

}
