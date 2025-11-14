package com.kipti.bnb.content.cogwheel_chain.graph;

import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlock;
import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.registry.BnbBlocks;
import com.simibubi.create.AllBlocks;
import net.createmod.catnip.data.Pair;
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

    List<ChainPathNode> nodes = new ArrayList<>();
    List<ChainPathCogwheelNode> cogwheelNodes = new ArrayList<>();

    public CogwheelChain(CompoundTag tag) {
        read(tag);
    }

    public CogwheelChain(PartialCogwheelChain source) throws InvalidGeometryException {
//        this.nodes = CogwheelChainGeometryBuilder.buildFullChainFromPartial(source);
        Pair<List<CogwheelChainPathfinder.PathNode>, List<ChainPathCogwheelNode>> pathNodes = CogwheelChainPathfinder.buildChainPath(source);
        if (pathNodes == null) {
            throw new InvalidGeometryException("Couldn't build a valid path, try inserting more nodes.");
        }
        this.nodes = CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(pathNodes.getFirst());
        this.cogwheelNodes = pathNodes.getSecond();
    }

    public @Nullable ChainPathCogwheelNode getNodeFromControllerOffset(Vec3i controllerOffset) {
        Vec3i offsetFromStart = controllerOffset.multiply(-1);

        for (ChainPathCogwheelNode cogwheelNode : cogwheelNodes) {
            if (cogwheelNode.offsetFromStart().equals(offsetFromStart)) {
                return cogwheelNode;
            }
        }
        return null;
    }

    public static class InvalidGeometryException extends Exception {
        public InvalidGeometryException(String message) {
            super(message);
        }
    }

    public void write(CompoundTag tag) {
        for (int i = 0; i < nodes.size(); i++) {
            ChainPathNode node = nodes.get(i);
            CompoundTag nodeTag = new CompoundTag();
            node.write(nodeTag);
            tag.put("node_" + i, nodeTag);
        }
        tag.putInt("node_count", nodes.size());

        for (int i = 0; i < cogwheelNodes.size(); i++) {
            ChainPathCogwheelNode pos = cogwheelNodes.get(i);
            CompoundTag posTag = new CompoundTag();
            pos.write(posTag);
            tag.put("cogwheel_pos_" + i, posTag);
        }
        tag.putInt("cogwheel_pos_count", cogwheelNodes.size());
    }

    public void read(CompoundTag tag) {
        nodes.clear();
        int nodeCount = tag.getInt("node_count");
        for (int i = 0; i < nodeCount; i++) {
            CompoundTag nodeTag = tag.getCompound("node_" + i);
            ChainPathNode node = ChainPathNode.read(nodeTag);
            nodes.add(node);
        }

        cogwheelNodes.clear();
        int cogWheelPosCount = tag.getInt("cogwheel_pos_count");
        for (int i = 0; i < cogWheelPosCount; i++) {
            CompoundTag posTag = tag.getCompound("cogwheel_pos_" + i);
            ChainPathCogwheelNode pos = ChainPathCogwheelNode.read(posTag);
            cogwheelNodes.add(pos);
        }
    }

    protected ChainPathNode getLastNode() {
        if (nodes.isEmpty()) {
            return null;
        }
        return nodes.getLast();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CogwheelChain that = (CogwheelChain) o;
        return Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodes);
    }

    public void placeInLevel(Level level, PartialCogwheelChain source) {
        boolean isController = true;
        BlockPos controllerPos = source.getFirstNode().pos();
        for (PartialCogwheelChainNode node : source.visitedNodes) {
            placeChainCogwheelInLevel(level, node, isController, controllerPos);
            isController = false;
        }

    }

    private void placeChainCogwheelInLevel(Level level, PartialCogwheelChainNode node, boolean isController, BlockPos controllerPos) {
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
        for (ChainPathCogwheelNode cogwheel : cogwheelNodes) {
            BlockPos pos = worldPosition.offset(cogwheel.offsetFromStart());
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
    public List<ChainPathNode> getChainPathNodes() {
        return nodes;
    }

    /**
     * Each cogwheel in the chain
     */
    public List<ChainPathCogwheelNode> getChainPathCogwheelNodes() {
        return cogwheelNodes;
    }
}
