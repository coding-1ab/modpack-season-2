package com.kipti.bnb.foundation;

import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChainGeometryBuilder;
import com.kipti.bnb.content.cogwheel_chain.graph.PartialCogwheelChainNode;
import com.kipti.bnb.content.cogwheel_chain.item.CogwheelChainPlacementEffect;
import com.kipti.bnb.content.girder_strut.GirderStrutPlacementEffects;
import com.kipti.bnb.content.weathered_girder.WeatheredGirderWrenchBehaviour;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    static final List<Pair<Vec3, Vec3>> deferredDebugRenderOutlines = Collections.synchronizedList(new ArrayList<>());

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        WeatheredGirderWrenchBehaviour.tick();

        //Render deferred debug outlines
        synchronized (deferredDebugRenderOutlines) {
            for (Pair<Vec3, Vec3> outline : deferredDebugRenderOutlines) {
                Outliner.getInstance().showLine(outline, outline.getFirst(), outline.getSecond());
            }
        }
    }

    public static void pushNewDeferredDebugRenderOutline(Pair<Vec3, Vec3> outline) {
        //Synchronized list to avoid concurrent modification exceptions
        synchronized (deferredDebugRenderOutlines) {
            deferredDebugRenderOutlines.add(outline);
        }
    }

    public static void clearDeferredDebugRenderOutlines() {
        synchronized (deferredDebugRenderOutlines) {
            deferredDebugRenderOutlines.clear();
        }
    }

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        //If in a level, there is a player, and the player is holding a girder strut block item, update the preview
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            GirderStrutPlacementEffects.tick(mc.player);
            CogwheelChainPlacementEffect.tick(mc.player);
        }

        var a = CogwheelChainGeometryBuilder.getTangentPointOnCircle(new Vec3(0, 1, 0), new Vec3(5, 0, 0),
            new PartialCogwheelChainNode(new BlockPos(0, 0, 0), Direction.Axis.Y, true),
            1,
            new PartialCogwheelChainNode(new BlockPos(5, 0, 0), Direction.Axis.Y, false),
            -1, false);

//
//        //Debugging for the cogwheel chain geo builder
//        List<PartialCogwheelChainNode> chain = List.of(
//
//            new PartialCogwheelChainNode(new BlockPos(2, 0, 0), Direction.Axis.Z, true),
//            new PartialCogwheelChainNode(new BlockPos(6, 0, 0), Direction.Axis.Z, true),
//            new PartialCogwheelChainNode(new BlockPos(8, 0, 0), Direction.Axis.Z, true),
//            new PartialCogwheelChainNode(new BlockPos(3, 2, 0), Direction.Axis.Z, true)
//
////
////            new PartialCogwheelChainNode(new BlockPos(5, 2, 5), Direction.Axis.Y, true),
////            new PartialCogwheelChainNode(new BlockPos(8, 1, 4), Direction.Axis.Z, true),
////            new PartialCogwheelChainNode(new BlockPos(11, 3, 4), Direction.Axis.Z, true),
////            new PartialCogwheelChainNode(new BlockPos(13, 3, 4), Direction.Axis.Z, true),
////            new PartialCogwheelChainNode(new BlockPos(16, 2, 5), Direction.Axis.Y, true),
////            new PartialCogwheelChainNode(new BlockPos(10, 2, 10), Direction.Axis.Y, true),
////            new PartialCogwheelChainNode(new BlockPos(5, 2, 10), Direction.Axis.Y, true)
//////            new PartialCogwheelChainNode(new BlockPos(0, 1, -1), Direction.Axis.Z, true),
//////            new PartialCogwheelChainNode(new BlockPos(-1, -2, 0), Direction.Axis.X, true),
//////            new PartialCogwheelChainNode(new BlockPos(3, 0, 3), Direction.Axis.Y, true)
//        );
//
//        List<CogwheelChainGeometryBuilderV3.PathNode> pathNodes = CogwheelChainGeometryBuilderV3.buildChainPath(new PartialCogwheelChain(chain));
//
//        if (pathNodes == null) {
//
//            ArrayList<CogwheelChainGeometryBuilderV3.PathNode> leftPath = new ArrayList<>();
//            for (PartialCogwheelChainNode chainNode : chain) {
//                leftPath.add(new CogwheelChainGeometryBuilderV3.PathNode(chainNode, 1));
//            }
//            renderstretch(leftPath);
//
//            ArrayList<CogwheelChainGeometryBuilderV3.PathNode> rightPath = new ArrayList<>();
//            for (PartialCogwheelChainNode chainNode : chain) {
//                rightPath.add(new CogwheelChainGeometryBuilderV3.PathNode(chainNode, -1));
//            }
//            renderstretch(rightPath);
//        } else {
//            renderstretch(pathNodes);
//
//        }
//
//        //Show a white line for each valid path
////
////        for (int from = -1; from <= 1; from += 2) {
////            for (int to = -1; to <= 1; to += 2) {
////                for (int i = 0; i < chain.size(); i++) {
////                    PartialCogwheelChainNode nodeFrom = chain.get(i);
////                    PartialCogwheelChainNode nodeTo = chain.get((i + 1) % chain.size());
////
////                    if (CogwheelChainGeometryBuilderV3.isValidPathStep(nodeFrom, from, nodeTo, to)) {
////                        Vec3 tangentFrom = CogwheelChainGeometryBuilderV3.getPathingTangentOnCog(
////                            nodeTo,
////                            nodeFrom,
////                            -from
////                        );
////                        Vec3 tangentTo = CogwheelChainGeometryBuilderV3.getPathingTangentOnCog(
////                            nodeFrom,
////                            nodeTo,
////                            to
////                        );
////                        Outliner.getInstance().showLine(
////                            "cogwheel_chain_debug_line_v3_" + (from == 1 ? "pos" : "neg") + "_" + (to == 1 ? "pos" : "neg") + "_" + i,
////                            tangentFrom.add(nodeFrom.pos().getCenter()),
////                            tangentTo.add(nodeTo.pos().getCenter())
////                        ).colored(0xFFFFFF);
////
//////                        Outliner.getInstance().showAABB(
//////                            "cogwheel_chain_debug_to_side_line_v3_" + (to == 1 ? "pos" : "neg") + "_" + (to == 1 ? "pos" : "neg") + "_" + i,
//////                            new AABB(tangentTo.add(nodeTo.pos().getCenter()).subtract(0.2, to == 1 ? 0.2 : 0.3, 0.2), tangentTo.add(nodeTo.pos().getCenter()).add(0.2, 0.2, 0.2))
//////                        ).colored(to == 1 ? 0x00FF00 : 0x0000FF);
//////
//////                        Outliner.getInstance().showAABB(
//////                            "cogwheel_chain_debug_from_side_line_v3_" + (from == 1 ? "pos" : "neg") + "_" + (to == 1 ? "pos" : "neg") + "_" + i,
//////                            new AABB(tangentFrom.add(nodeFrom.pos().getCenter()).subtract(0.1, from == 1 ? 0.1 : 0.25, 0.1), tangentFrom.add(nodeFrom.pos().getCenter()).add(0.1, 0.1, 0.1))
//////                        ).colored(from == 1 ? 0x00FF00 : 0x0000FF);
////                    }
////                }
////            }
////        }
//
//
//        //
//////        for (int i = 0; i < chain.getNodes().size(); i++) {
//////            Outliner.getInstance().showLine(
//////                "cogwheel_chain_debug_line_" + i,
//////                chain.getNodes().get(i).getPosition(),
//////                chain.getNodes().get((i + 1) % chain.getNodes().size()).getPosition()
//////            );
//////       }
////
////
////        //Show the red (1 -> 1), green (-1 -> -1), blue (1 -> -1) and yellow (-1 -> 1) connections between each pair of chainNode
////        for (int i = 0; i < chain.size(); i++) {
////            PartialCogwheelChainNode nodeA = chain.get(i);
////            PartialCogwheelChainNode nodeB = chain.get((i + 1) % chain.size());
////
////            displayCogwheelLine(i, "red", nodeB, nodeA, 1, 1, 0xFF0000);
////            displayCogwheelLine(i, "green", nodeB, nodeA, -1, -1, 0x00FF00);
////            displayCogwheelLine(i, "blue", nodeB, nodeA, 1, -1, 0x0000FF);
////            displayCogwheelLine(i, "yellow", nodeB, nodeA, -1, 1, 0xff00FF);
////
////            Vec3 axis = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(nodeA.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
////        }
    }

//    private static void renderstretch(List<CogwheelChainGeometryBuilderV3.PathNode> pathNodes) {
//        for (int i = 0; i < pathNodes.size(); i++) {
//            CogwheelChainGeometryBuilderV3.PathNode fromNode = pathNodes.get(i);
//            CogwheelChainGeometryBuilderV3.PathNode toNode = pathNodes.get((i + 1) % pathNodes.size());
//            Vec3 toPos = CogwheelChainGeometryBuilderV3.getPathingTangentOnCog(fromNode.chainNode(), toNode.chainNode(), toNode.sideFactor()).add(toNode.chainNode().pos().getCenter());
//            Vec3 fromPos = CogwheelChainGeometryBuilderV3.getPathingTangentOnCog(toNode.chainNode(), fromNode.chainNode(), -fromNode.sideFactor()).add(fromNode.chainNode().pos().getCenter());
//            Outliner.getInstance().showLine(
//                "cogwheel_chain_debug_line_path_" + i + " sideFactor " + toNode.sideFactor(),
//                fromPos,
//                toPos
//            ).colored(0xFFFF00).lineWidth(0.2f);
//
//            int toNodeColor = toNode.sideFactor() == 1 ? 0x00FF00 : 0x0000FF;
//            Outliner.getInstance().showAABB("cogwheel_chain_debug_aabb_to_" + i + " sideFactor " + toNode.sideFactor(),
//                new AABB(toPos.subtract(0.1, 0.1, 0.1), toPos.add(0.1, 0.1, 0.1))
//            ).colored(toNodeColor);
//
//            int fromNodeColor = fromNode.sideFactor() == 1 ? 0x00FF00 : 0x0000FF;
//            Outliner.getInstance().showAABB("cogwheel_chain_debug_aabb_from_" + i + " sideFactor " + toNode.sideFactor(),
//                new AABB(fromPos.subtract(0.1, 0.1, 0.1), fromPos.add(0.1, 0.1, 0.1))
//            ).colored(fromNodeColor);
//            Outliner.getInstance().showAABB("cogwheel_chain_node_" + i,
//                new AABB(toNode.chainNode().pos().getCenter().subtract(0.1, 0.1, 0.1), toNode.chainNode().pos().getCenter().add(0.1, 0.1, 0.1))
//            ).colored(0xff8800);
//            Outliner.getInstance().showAABB("cogwheel_chain_axis_" + i,
//                new AABB(toNode.chainNode().pos().getCenter().subtract(0.05, 0.05, 0.0).subtract(toNode.chainNode().axis()), toNode.chainNode().pos().getCenter().add(0.05, 0.05, 0.05).add(toNode.chainNode().axis()))
//            ).colored(0xff8800);
//        }
//    }
//
//    private static void displayCogwheelLine(int i, String name, PartialCogwheelChainNode nodeA, PartialCogwheelChainNode nodeB, int signA, int signB, int color) {
//        Vec3 tangentFrom = CogwheelChainGeometryBuilderV3.getPathingTangentOnCog(
//            nodeB,
//            nodeA,
//            -signA
//        );
//        Vec3 tangentTo = CogwheelChainGeometryBuilderV3.getPathingTangentOnCog(
//            nodeA,
//            nodeB,
//            signB
//        );
//        if (tangentFrom == null || tangentTo == null) {
//            return;
//        }
//        Outliner.getInstance().showLine(
//            "cogwheel_chain_debug_line_" + name + i,
//            tangentFrom.add(nodeB.center()),
//            tangentTo.add(nodeA.center())
//        ).colored(color);
//    }

}
