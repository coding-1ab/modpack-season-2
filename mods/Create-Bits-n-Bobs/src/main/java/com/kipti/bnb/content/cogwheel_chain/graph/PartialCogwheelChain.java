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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public boolean tryAddNode(Level level, BlockPos newPos, BlockState newBlockState) throws ChainAdditionAbortedException {
        PartialCogwheelChainNode lastNode = getLastNode();

        if (!PartialCogwheelChain.isValidBlockTarget(level, newPos, newBlockState)) {
            return false;
        }

        Direction.Axis axis = newBlockState.getValue(CogWheelBlock.AXIS);
        boolean isLarge = newBlockState.getBlock() instanceof ICogWheel iCogWheel && iCogWheel.isLargeCog();

        int differenceOnAxis = Math.abs(newPos.get(axis) - lastNode.pos().get(axis));
        @Nullable PartialCogwheelChainNode lastLastNode = getSize() >= 2 ? visitedNodes.get(visitedNodes.size() - 2) : null;
        boolean isPrecededByAxisChange = lastLastNode != null && lastLastNode.rotationAxis() != lastNode.rotationAxis();

        boolean isFlat = differenceOnAxis == 0;
        boolean isSameAxis = axis == lastNode.rotationAxis();
        double totalRadius = (isLarge ? 1 : 0.5) + (lastNode.isLarge() ? 1 : 0.5);
        boolean isAdjacent = isFlat && newPos.distSqr(lastNode.pos()) <= totalRadius * totalRadius;
        boolean isValidFlat = isSameAxis && isFlat && !isAdjacent;
        boolean isValidByConsecutiveChange = !isPrecededByAxisChange || isValidConsecutiveAxisChange(lastLastNode, lastNode, newPos, axis);
        boolean isValidAxisChange = isValidLargeCogConnection(lastNode, newPos, axis) && isValidByConsecutiveChange;
        boolean isValid = isValidFlat || isValidAxisChange;

        if (!isValid) {
            if (isAdjacent) {
                throw new ChainAdditionAbortedException("Cogwheels must not touch!");
            }

            if (!isSameAxis) {
                if (!isValidByConsecutiveChange) {
                    throw new ChainAdditionAbortedException("Invalid axis change, must be on same side of pivot!");
                } else {
                    throw new ChainAdditionAbortedException("Not a valid axis change!");
                }
            }
            //Else it wasn't accepted cause it wasnt flat
            throw new ChainAdditionAbortedException("Connection must be flat when on the same axis!");
        }

        PartialCogwheelChainNode newNode = new PartialCogwheelChainNode(
            newPos, axis, isLarge
        );

        visitedNodes.add(newNode);
        return true;
    }

    private boolean isValidConsecutiveAxisChange(@NotNull PartialCogwheelChainNode lastNode, PartialCogwheelChainNode pivotNode, BlockPos newPos, Direction.Axis axis) {
        //Get the signed difference to the pivot on the node's rotation axis
        int diffToPivotOnLastNodeAxis = lastNode.pos().get(lastNode.rotationAxis()) - pivotNode.pos().get(lastNode.rotationAxis());

        //Get the signed difference to the pivot on the new node's rotation axis
        int diffToPivotOnNewNodeAxis = newPos.get(axis) - pivotNode.pos().get(axis);

        if (diffToPivotOnLastNodeAxis == diffToPivotOnNewNodeAxis) {
            return true;
        }

        //Check if it's like a wrap around the pivot, in which case its safe
        //Get the other axis, and if they are on the same side along this other axis
        int safeAxisOrdinal = Integer.numberOfTrailingZeros(7 & ~(1 << axis.ordinal()) & ~(1 << lastNode.rotationAxis().ordinal()));
        Direction.Axis safeAxis = Direction.Axis.values()[safeAxisOrdinal];

        int lastDiffOnSafeAxis = lastNode.pos().get(safeAxis) - pivotNode.pos().get(safeAxis);
        int newDiffOnSafeAxis = newPos.get(safeAxis) - pivotNode.pos().get(safeAxis);
        return Math.signum(lastDiffOnSafeAxis) == Math.signum(newDiffOnSafeAxis);
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

    public static class ChainAdditionAbortedException extends Exception {

        public ChainAdditionAbortedException(String message) {
            super(message);
        }

    }

}
