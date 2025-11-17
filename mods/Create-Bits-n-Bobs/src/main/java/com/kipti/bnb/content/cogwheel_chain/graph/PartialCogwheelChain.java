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
    public static final Integer MAX_CHAIN_BOUNDS = 32;

    List<PartialCogwheelChainNode> visitedNodes;

    public PartialCogwheelChain(BlockPos startPos, Direction.Axis startAxis, boolean isLarge) {
        this.visitedNodes = new ArrayList<>(List.of(new PartialCogwheelChainNode(startPos, startAxis, isLarge)));
    }

    public PartialCogwheelChain(List<PartialCogwheelChainNode> nodes) {
        visitedNodes = new ArrayList<>(nodes);
    }

    public static boolean isValidBlockTarget(final BlockState state) {
        return state.getBlock() instanceof final ICogWheel iCogWheel && iCogWheel.isDedicatedCogWheel();
    }

    //TODO: dimemsion check, try break down this logic
    public boolean tryAddNode(final BlockPos newPos, final BlockState newBlockState) throws ChainAdditionAbortedException {
        final PartialCogwheelChainNode lastNode = getLastNode();

        if (!isValidBlockTarget(newBlockState)) {
            return false;
        }

        Direction.Axis axis = newBlockState.getValue(CogWheelBlock.AXIS);
        boolean isLarge = newBlockState.getBlock() instanceof ICogWheel iCogWheel && iCogWheel.isLargeCog(); //TODO: replace with more explicit block check

        int differenceOnAxis = Math.abs(newPos.get(axis) - lastNode.pos().get(axis));
        @Nullable PartialCogwheelChainNode lastLastNode = getSize() >= 2 ? visitedNodes.get(visitedNodes.size() - 2) : null;
        boolean isPrecededByAxisChange = lastLastNode != null && lastLastNode.rotationAxis() != lastNode.rotationAxis();

        boolean isFlat = differenceOnAxis == 0;
        boolean isSameAxis = axis == lastNode.rotationAxis();
        double totalRadius = (isLarge ? 1 : 0.5) + (lastNode.isLarge() ? 1 : 0.5);
        boolean isAdjacent = isFlat && newPos.distSqr(lastNode.pos()) <= totalRadius * totalRadius;
        boolean isValidFlat = isSameAxis && isFlat && !isAdjacent;
//        boolean isValidByConsecutiveChange = !isPrecededByAxisChange || isValidConsecutiveAxisChange(lastLastNode, lastNode, newPos, axis);
        boolean isValidAxisChange = isValidLargeCogAxisConnection(lastNode, newPos, axis, isLarge);
        boolean isValid = isValidFlat || isValidAxisChange;

        if (!isValid) {
            if (isAdjacent) {
                throw new ChainAdditionAbortedException("Cogwheels must not touch!");
            }

            if (!isSameAxis) {
                throw new ChainAdditionAbortedException("Not a valid axis change!");
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
        //Get the signed difference to the pivot on the chainNode's rotation axis
        int diffToPivotOnLastNodeAxis = lastNode.pos().get(lastNode.rotationAxis()) - pivotNode.pos().get(lastNode.rotationAxis());

        //Get the signed difference to the pivot on the new chainNode's rotation axis
        int diffToPivotOnNewNodeAxis = newPos.get(axis) - pivotNode.pos().get(axis);

        if (diffToPivotOnLastNodeAxis == diffToPivotOnNewNodeAxis) {
            return true;
        }

        //Check if it's like a wrap around the pivot, in which case its safe
        //Get the other axis, and if they are on the same sideFactor along this other axis
        int safeAxisOrdinal = Integer.numberOfTrailingZeros(7 & ~(1 << axis.ordinal()) & ~(1 << lastNode.rotationAxis().ordinal()));
        Direction.Axis safeAxis = Direction.Axis.values()[safeAxisOrdinal];

        int lastDiffOnSafeAxis = lastNode.pos().get(safeAxis) - pivotNode.pos().get(safeAxis);
        int newDiffOnSafeAxis = newPos.get(safeAxis) - pivotNode.pos().get(safeAxis);
        return Math.signum(lastDiffOnSafeAxis) == Math.signum(newDiffOnSafeAxis);
    }

    private boolean isValidLargeCogAxisConnection(PartialCogwheelChainNode lastNode, BlockPos newPos, Direction.Axis axis, boolean isLarge) {
        if (!lastNode.isLarge() || !isLarge) {
            return true;
        }

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

    public boolean canBuildChainIfLooping() throws CogwheelChain.InvalidGeometryException {
        if (getSize() < 2) return false;
        final PartialCogwheelChainNode firstNode = visitedNodes.getFirst();
        final PartialCogwheelChainNode lastNode = getLastNode();
        if (!firstNode.pos().equals(lastNode.pos())) return false;

        // Remove last chainNode to avoid duplication
        visitedNodes.removeLast();
        if (CogwheelChainPathfinder.buildChainPath(this) == null) {
            throw new CogwheelChain.InvalidGeometryException();
        }
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

    public int maxBounds() {
        Vec3i min = new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vec3i max = new Vec3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (PartialCogwheelChainNode node : visitedNodes) {
            BlockPos pos = node.pos();
            min = new Vec3i(
                    Math.min(min.getX(), pos.getX()),
                    Math.min(min.getY(), pos.getY()),
                    Math.min(min.getZ(), pos.getZ())
            );
            max = new Vec3i(
                    Math.max(max.getX(), pos.getX()),
                    Math.max(max.getY(), pos.getY()),
                    Math.max(max.getZ(), pos.getZ())
            );
        }

        return Math.max(Math.max(max.getX() - min.getX(), max.getY() - min.getY()), max.getZ() - min.getZ());
    }

    public boolean checkMatchingNodesInLevel(Level level) {
        for (PartialCogwheelChainNode node : visitedNodes) {
            BlockState state = level.getBlockState(node.pos());
            if (!isValidBlockTarget(state)) {
                return false;
            }
            Direction.Axis axis = state.getValue(CogWheelBlock.AXIS);
            boolean isLarge = state.getBlock() instanceof ICogWheel iCogWheel && iCogWheel.isLargeCog();
            if (axis != node.rotationAxis() || isLarge != node.isLarge()) {
                return false;
            }
        }
        return true;
    }

    public PartialCogwheelChain toLocalSpaceChain() {
        final BlockPos origin = getFirstNode().pos();
        final List<PartialCogwheelChainNode> localNodes = new ArrayList<>();
        for (final PartialCogwheelChainNode node : visitedNodes) {
            final BlockPos localPos = node.pos().subtract(origin);
            localNodes.add(new PartialCogwheelChainNode(localPos, node.rotationAxis(), node.isLarge()));
        }
        return new PartialCogwheelChain(localNodes);
    }

    public static class ChainAdditionAbortedException extends Exception {

        public ChainAdditionAbortedException(String message) {
            super(message);
        }

    }

}
