package com.kipti.bnb.content.cogwheel_chain.graph;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Used during the construction of a cogwheel chain
 */
public class PartialCogwheelChain {

    public static final Codec<PartialCogwheelChain> CODEC = PartialCogwheelChainNode.CODEC.listOf().xmap(PartialCogwheelChain::new, chain -> chain.visitedNodes);

    public static final StreamCodec<RegistryFriendlyByteBuf, PartialCogwheelChain> STREAM_CODEC = StreamCodec.composite(
        CatnipStreamCodecBuilders.list(PartialCogwheelChainNode.STREAM_CODEC),
        chain -> chain.visitedNodes,
        PartialCogwheelChain::new
    );

    List<PartialCogwheelChainNode> visitedNodes;

    public PartialCogwheelChain(BlockPos startPos, Direction.Axis startAxis, boolean isLarge) {
        this.visitedNodes = new ArrayList<>(List.of(new PartialCogwheelChainNode(startPos, startAxis, isLarge)));
    }

    public PartialCogwheelChain(List<PartialCogwheelChainNode> nodes) {
        visitedNodes = new ArrayList<>(nodes);
    }

    public static boolean isValidBlockTarget(Level level, BlockPos clickedPos, BlockState state) {
        return state.getBlock() instanceof ICogWheel iCogWheel && iCogWheel.isDedicatedCogWheel();
    }

    public boolean tryAddNode(Level level, BlockPos newPos, BlockState newBlockState) {
        PartialCogwheelChainNode lastNode = getLastNode();

        if (!PartialCogwheelChain.isValidBlockTarget(level, newPos, newBlockState)) {
            return false;
        }

        Direction.Axis axis = newBlockState.getValue(CogWheelBlock.AXIS);
        boolean isLarge = newBlockState.getBlock() instanceof ICogWheel iCogWheel && iCogWheel.isLargeCog();

        boolean isValid = axis == lastNode.rotationAxis() || isValidLargeCogConnection(lastNode, newPos, axis);

        PartialCogwheelChainNode newNode = new PartialCogwheelChainNode(
            newPos, axis, isLarge
        );

        if (isValid) {
            visitedNodes.add(newNode);
            return true;
        } else {
            return false;
        }
    }

    private boolean isValidLargeCogConnection(PartialCogwheelChainNode lastNode, BlockPos newPos, Direction.Axis axis) {
        // Check that they are one block apart on the two axes perpendicular to the rotation axes
        Vec3i diff = newPos.subtract(lastNode.pos());

        int safeAxisOrdinal = 0x7 & ~(1 << axis.ordinal()) & ~(1 << lastNode.rotationAxis().ordinal());
        int[] component = {diff.getX(), diff.getY(), diff.getZ()};
        for (int i = 0; i < 3; i++) {
            if (0b1 << i == safeAxisOrdinal) {
                if (Math.abs(component[i]) <= 1) {
                    return false;
                }
            } else {
                if (Math.abs(component[i]) != 1) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PartialCogwheelChain that = (PartialCogwheelChain) o;
        return Objects.equals(visitedNodes, that.visitedNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(visitedNodes);
    }

    public boolean completeIfLooping(Level level) {
        if (getSize() < 2 || level.isClientSide) return false;
        PartialCogwheelChainNode firstNode = visitedNodes.getFirst();
        PartialCogwheelChainNode lastNode = getLastNode();
        if (!firstNode.pos().equals(lastNode.pos())) return false;

        // Remove last node to avoid duplication
        visitedNodes.removeLast();
        CogwheelChain completedChain = new CogwheelChain(this);
        completedChain.placeInLevel(level, this);
        return true;
    }

    public List<PartialCogwheelChainNode> getNodes() {
        return visitedNodes;
    }

    public PartialCogwheelChainNode getNodeLooped(int i) {
        return visitedNodes.get((visitedNodes.size() + (i % visitedNodes.size())) % visitedNodes.size());
    }

    public PartialCogwheelChainNode getFirstNode() {
        return visitedNodes.getFirst();
    }

    public PartialCogwheelChainNode getLastNode() {
        return visitedNodes.getLast();
    }

    public Vec3 getNodeCenter(int i) {
        return visitedNodes.get(i).pos().getCenter();
    }

    public int getSize() {
        return visitedNodes.size();
    }

}
