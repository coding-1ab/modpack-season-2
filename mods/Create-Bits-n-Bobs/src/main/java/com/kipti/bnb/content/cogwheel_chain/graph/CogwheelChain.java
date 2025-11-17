package com.kipti.bnb.content.cogwheel_chain.graph;

import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlock;
import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.registry.BnbBlocks;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CogwheelChain {

    private final List<PathedCogwheelNode> cogwheelNodes;
    private List<RenderedChainPathNode> renderedNodes;

    public CogwheelChain(final CompoundTag tag) {
        renderedNodes = new ArrayList<>();
        cogwheelNodes = new ArrayList<>();
        read(tag);
    }

    public CogwheelChain(final List<PathedCogwheelNode> path) {
        this.cogwheelNodes = path;
        this.renderedNodes = CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(path);
    }

    public @Nullable PathedCogwheelNode getNodeFromControllerOffset(final Vec3i controllerOffset) {
        final Vec3i offsetFromStart = controllerOffset.multiply(-1);

        for (final PathedCogwheelNode cogwheelNode : cogwheelNodes) {
            if (cogwheelNode.localPos().equals(offsetFromStart)) {
                return cogwheelNode;
            }
        }
        return null;
    }

    public static class InvalidGeometryException extends Exception {
        public InvalidGeometryException(final String reason) {
            super(reason);
        }
    }

    public void write(final CompoundTag tag) {
        tag.putInt("cogwheel_pos_count", cogwheelNodes.size());
        for (int i = 0; i < cogwheelNodes.size(); i++) {
            final CompoundTag posTag = new CompoundTag();
            cogwheelNodes.get(i).write(posTag);
            tag.put("cogwheel_pos_" + i, posTag);
        }
    }

    public void read(CompoundTag tag) {
        cogwheelNodes.clear();
        final int cogWheelPosCount = tag.getInt("cogwheel_pos_count");
        for (int i = 0; i < cogWheelPosCount; i++) {
            final CompoundTag posTag = tag.getCompound("cogwheel_pos_" + i);
            final PathedCogwheelNode pos = PathedCogwheelNode.read(posTag);
            cogwheelNodes.add(pos);
        }
        renderedNodes.clear();
        renderedNodes = CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(cogwheelNodes);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final CogwheelChain that = (CogwheelChain) o;
        return Objects.equals(renderedNodes, that.renderedNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(renderedNodes);
    }

    public void placeInLevel(final Level level, final PlacingCogwheelChain source) {
        boolean isController = true;
        final BlockPos controllerPos = source.getFirstNode().pos();
        for (final PlacingCogwheelNode node : source.visitedNodes) {
            placeChainCogwheelInLevel(level, node, isController, controllerPos);
            isController = false;
        }

    }

    private void placeChainCogwheelInLevel(final Level level, PlacingCogwheelNode node, boolean isController, BlockPos controllerPos) {
        level.setBlockAndUpdate(node.pos(), (node.isLarge() ? BnbBlocks.LARGE_COGWHEEL_CHAIN : BnbBlocks.SMALL_COGWHEEL_CHAIN).getDefaultState()
                .setValue(CogwheelChainBlock.AXIS, node.rotationAxis()));

        BlockEntity be = level.getBlockEntity(node.pos());
        if (be instanceof CogwheelChainBlockEntity chainBE) {
            if (isController) {
                chainBE.setAsController(this);
            } else {
                chainBE.setController(controllerPos.subtract(node.pos()));
            }
        } else {
            throw new IllegalStateException("Expected CogwheelChainBlockEntity at " + node.pos());
        }
    }

    public void destroy(Level level, BlockPos worldPosition) {
        for (PathedCogwheelNode cogwheel : cogwheelNodes) {
            BlockPos pos = worldPosition.offset(cogwheel.localPos());
            removeChainCogwheelFromLevelIfPresent(level, pos);
        }
    }

    private void removeChainCogwheelFromLevelIfPresent(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        if (be instanceof CogwheelChainBlockEntity && (state.getBlock() instanceof CogwheelChainBlock cogwheelChainBlock)) {
            level.setBlockAndUpdate(pos, (cogwheelChainBlock.isLargeChainCog() ? AllBlocks.LARGE_COGWHEEL : AllBlocks.COGWHEEL).getDefaultState()
                    .setValue(CogwheelChainBlock.AXIS, state.getValue(CogwheelChainBlock.AXIS)));
        }
    }

    /**
     * All nodes in the chain, there are typically multiple, as the path wraps around cogwheels
     */
    public List<RenderedChainPathNode> getChainPathNodes() {
        return renderedNodes;
    }

    /**
     * Each cogwheel in the chain
     */
    public List<PathedCogwheelNode> getChainPathCogwheelNodes() {
        return cogwheelNodes;
    }
}
