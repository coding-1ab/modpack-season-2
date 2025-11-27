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
public class PlacingCogwheelChain {

    public static final Codec<PlacingCogwheelChain> CODEC = PlacingCogwheelNode.CODEC.listOf().xmap(PlacingCogwheelChain::new, chain -> chain.visitedNodes);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlacingCogwheelChain> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(PlacingCogwheelNode.STREAM_CODEC),
            chain -> chain.visitedNodes,
            PlacingCogwheelChain::new
    );
    public static final Integer MAX_CHAIN_BOUNDS = 32;

    List<PlacingCogwheelNode> visitedNodes;

    public PlacingCogwheelChain(final BlockPos startPos, final Direction.Axis startAxis, final boolean isLarge) {
        this.visitedNodes = new ArrayList<>(List.of(new PlacingCogwheelNode(startPos, startAxis, isLarge)));
    }

    public PlacingCogwheelChain(final List<PlacingCogwheelNode> nodes) {
        visitedNodes = new ArrayList<>(nodes);
    }

    public int getChainsRequired() {
        double length = 0;
        for (int i = 0; i < visitedNodes.size() - 1; i++) {
            final Vec3i offset = visitedNodes.get(i + 1).pos().subtract(visitedNodes.get(i).pos());
            length += Vec3.atLowerCornerOf(offset).length();
        }
        return (int) Math.max(Math.round(length / 2.5), 1);
    }

    public static boolean isValidBlockTarget(final BlockState state) {
        return state.getBlock() instanceof final ICogWheel iCogWheel && iCogWheel.isDedicatedCogWheel();
    }

    //TODO: dimemsion check, try break down this logic
    public boolean tryAddNode(final BlockPos newPos, final BlockState newBlockState) throws ChainAdditionAbortedException {
        final PlacingCogwheelNode lastNode = getLastNode();

        if (!isValidBlockTarget(newBlockState)) {
            return false;
        }

        final Direction.Axis axis = newBlockState.getValue(CogWheelBlock.AXIS);
        final boolean isLarge = newBlockState.getBlock() instanceof final ICogWheel iCogWheel && iCogWheel.isLargeCog(); //TODO: replace with more explicit block check

        final int differenceOnAxis = Math.abs(newPos.get(axis) - lastNode.pos().get(axis));
        @Nullable final PlacingCogwheelNode lastLastNode = getSize() >= 2 ? visitedNodes.get(visitedNodes.size() - 2) : null;
        final boolean isPrecededByAxisChange = lastLastNode != null && lastLastNode.rotationAxis() != lastNode.rotationAxis();

        final boolean isFlat = differenceOnAxis == 0;
        final boolean isSameAxis = axis == lastNode.rotationAxis();
        final double totalRadius = (isLarge ? 1 : 0.5) + (lastNode.isLarge() ? 1 : 0.5);
        final boolean isAdjacent = isFlat && newPos.distSqr(lastNode.pos()) <= totalRadius * totalRadius;
        final boolean isValidFlat = isSameAxis && isFlat && !isAdjacent;
//        boolean isValidByConsecutiveChange = !isPrecededByAxisChange || isValidConsecutiveAxisChange(lastLastNode, lastNode, newPos, axis);
        final boolean isValidAxisChange = isValidLargeCogAxisConnection(lastNode, newPos, axis, isLarge);
        final boolean isValid = isValidFlat || isValidAxisChange;

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

        final PlacingCogwheelNode newNode = new PlacingCogwheelNode(
                newPos, axis, isLarge
        );

        visitedNodes.add(newNode);
        return true;
    }

    private boolean isValidConsecutiveAxisChange(@NotNull final PlacingCogwheelNode lastNode, final PlacingCogwheelNode pivotNode, final BlockPos newPos, final Direction.Axis axis) {
        //Get the signed difference to the pivot on the chainNode's rotation axis
        final int diffToPivotOnLastNodeAxis = lastNode.pos().get(lastNode.rotationAxis()) - pivotNode.pos().get(lastNode.rotationAxis());

        //Get the signed difference to the pivot on the new chainNode's rotation axis
        final int diffToPivotOnNewNodeAxis = newPos.get(axis) - pivotNode.pos().get(axis);

        if (diffToPivotOnLastNodeAxis == diffToPivotOnNewNodeAxis) {
            return true;
        }

        //Check if it's like a wrap around the pivot, in which case its safe
        //Get the other axis, and if they are on the same sideFactor along this other axis
        final int safeAxisOrdinal = Integer.numberOfTrailingZeros(7 & ~(1 << axis.ordinal()) & ~(1 << lastNode.rotationAxis().ordinal()));
        final Direction.Axis safeAxis = Direction.Axis.values()[safeAxisOrdinal];

        final int lastDiffOnSafeAxis = lastNode.pos().get(safeAxis) - pivotNode.pos().get(safeAxis);
        final int newDiffOnSafeAxis = newPos.get(safeAxis) - pivotNode.pos().get(safeAxis);
        return Math.signum(lastDiffOnSafeAxis) == Math.signum(newDiffOnSafeAxis);
    }

    private boolean isValidLargeCogAxisConnection(final PlacingCogwheelNode lastNode, final BlockPos newPos, final Direction.Axis axis, final boolean isLarge) {
        if (!lastNode.isLarge() || !isLarge) {
            return true;
        }

        // Check that they are one block apart on the two axes perpendicular to the rotation axes
        final Vec3i diff = newPos.subtract(lastNode.pos());

        final int safeAxisOrdinal = 0x7 & ~(1 << axis.ordinal()) & ~(1 << lastNode.rotationAxis().ordinal());
        final int[] component = {diff.getX(), diff.getY(), diff.getZ()};
        for (int i = 0; i < 3; i++) {
            if (0b1 << i == safeAxisOrdinal) {
                if (Math.abs(component[i]) < 1) {
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
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final PlacingCogwheelChain that = (PlacingCogwheelChain) o;
        return Objects.equals(visitedNodes, that.visitedNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(visitedNodes);
    }

    public boolean canBuildChainIfLooping() throws CogwheelChain.InvalidGeometryException {
        if (getSize() < 2) return false;
        final PlacingCogwheelNode firstNode = visitedNodes.getFirst();
        final PlacingCogwheelNode lastNode = getLastNode();
        if (!firstNode.pos().equals(lastNode.pos())) return false;

        // Remove last chainNode to avoid duplication
        visitedNodes.removeLast();
        if (CogwheelChainPathfinder.buildChainPath(this) == null) {
            throw new CogwheelChain.InvalidGeometryException("try_inserting_more_nodes");
        }
        return true;
    }

    public List<PlacingCogwheelNode> getNodes() {
        return visitedNodes;
    }

    public PlacingCogwheelNode getNodeLooped(final int i) {
        return visitedNodes.get((visitedNodes.size() + (i % visitedNodes.size())) % visitedNodes.size());
    }

    public PlacingCogwheelNode getFirstNode() {
        return visitedNodes.getFirst();
    }

    public PlacingCogwheelNode getLastNode() {
        return visitedNodes.getLast();
    }

    public Vec3 getNodeCenter(final int i) {
        return visitedNodes.get(i).pos().getCenter();
    }

    public int getSize() {
        return visitedNodes.size();
    }

    public int maxBounds() {
        Vec3i min = new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vec3i max = new Vec3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (final PlacingCogwheelNode node : visitedNodes) {
            final BlockPos pos = node.pos();
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

    public boolean checkMatchingNodesInLevel(final Level level) {
        for (final PlacingCogwheelNode node : visitedNodes) {
            final BlockState state = level.getBlockState(node.pos());
            if (!isValidBlockTarget(state)) {
                return false;
            }
            final Direction.Axis axis = state.getValue(CogWheelBlock.AXIS);
            final boolean isLarge = state.getBlock() instanceof final ICogWheel iCogWheel && iCogWheel.isLargeCog();
            if (axis != node.rotationAxis() || isLarge != node.isLarge()) {
                return false;
            }
        }
        return true;
    }

    public PlacingCogwheelChain toLocalSpaceChain() {
        final BlockPos origin = getFirstNode().pos();
        final List<PlacingCogwheelNode> localNodes = new ArrayList<>();
        for (final PlacingCogwheelNode node : visitedNodes) {
            final BlockPos localPos = node.pos().subtract(origin);
            localNodes.add(new PlacingCogwheelNode(localPos, node.rotationAxis(), node.isLarge()));
        }
        return new PlacingCogwheelChain(localNodes);
    }

    public static class ChainAdditionAbortedException extends Exception {

        public ChainAdditionAbortedException(final String message) {
            super(message);
        }

    }

}
