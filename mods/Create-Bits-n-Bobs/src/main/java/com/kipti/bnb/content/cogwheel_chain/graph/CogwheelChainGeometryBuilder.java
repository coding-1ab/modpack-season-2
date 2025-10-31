package com.kipti.bnb.content.cogwheel_chain.graph;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CogwheelChainGeometryBuilder {

    public static List<CogwheelChainNode> buildFullChainFromPartial(PartialCogwheelChain source) {
        List<PartialCogwheelChainNode> sourceNodes = source.getNodes();
        List<CogwheelChainNode> resultNodes = new ArrayList<>();

        for (int i = 0; i < sourceNodes.size(); i++) {
            PartialCogwheelChainNode previousNode = source.getNodeLooped(i - 1);
            PartialCogwheelChainNode sourceNode = sourceNodes.get(i);
            PartialCogwheelChainNode nextNode = source.getNodeLooped(i + 1);

            Pair<Vec3, Vec3> nodeTo = getNode(previousNode, sourceNode, nextNode);

            resultNodes.add(new CogwheelChainNode(sourceNode.pos(), nodeTo.getFirst()));
            resultNodes.add(new CogwheelChainNode(sourceNode.pos(), nodeTo.getSecond()));
        }

        return resultNodes;
    }

    private static Pair<Vec3, Vec3> getNode(PartialCogwheelChainNode previousNode, PartialCogwheelChainNode currentNode, PartialCogwheelChainNode nextNode) {
        Vec3 axis = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(currentNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());

        Vec3 incomingDiff = currentNode.pos().getCenter().subtract(previousNode.pos().getCenter());
        Vec3 outgoingDiff = nextNode.pos().getCenter().subtract(currentNode.pos().getCenter());

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            Vec3 previousAxis = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(previousNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
            incomingDiff = incomingDiff.subtract(previousAxis.scale(incomingDiff.dot(previousAxis)));
        }
        if (nextNode.rotationAxis() != currentNode.rotationAxis()) {
            Vec3 nextAxis = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(nextNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
            outgoingDiff = outgoingDiff.subtract(nextAxis.scale(outgoingDiff.dot(nextAxis)));
        }


        Vec3 incoming = incomingDiff.normalize();
        Vec3 outgoing = outgoingDiff.normalize();

        incoming = incoming.subtract(axis.scale(incoming.dot(axis))).normalize();
        outgoing = outgoing.subtract(axis.scale(outgoing.dot(axis))).normalize();

        double dot = axis.dot(incoming.cross(outgoing));
        double concaveSign = dot == 0 ? -1 : Math.signum(dot);
        double radius = currentNode.isLarge() ? 1.0f : 0.5f;

        if (previousNode.rotationAxis() != currentNode.rotationAxis() || nextNode.rotationAxis() != currentNode.rotationAxis()) {
//            concaveSign *= -1; //Use a concave sign
        }

        Vec3 incomingOffset = incoming.cross(axis).normalize().scale(concaveSign).scale(radius);
        Vec3 outgoingOffset = outgoing.cross(axis).normalize().scale(concaveSign).scale(radius);

//
//    / /        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
//    / /            Direction.Axis prevRotationAxis = previousNode.rotationAxis();
//    / /            incomingOffset = Vec3.ZERO.relative(Direction.fromAxisAndDirection(prevRotationAxis, Direction.AxisDirection.POSITIVE), incomingDiff.get(prevRotationAxis));
//    / /        }
//    / /        if (nextNode.rotationAxis() != currentNode.rotationAxis()) {
//    / /            Direction.Axis nextRotationAxis = nextNode.rotationAxis();
//    / /            outgoingOffset = Vec3.ZERO.relative(Direction.fromAxisAndDirection(nextRotationAxis, Direction.AxisDirection.POSITIVE), outgoingDiff.get(nextRotationAxis));
//    / /        }

        return Pair.of(incomingOffset, outgoingOffset);
    }
}
