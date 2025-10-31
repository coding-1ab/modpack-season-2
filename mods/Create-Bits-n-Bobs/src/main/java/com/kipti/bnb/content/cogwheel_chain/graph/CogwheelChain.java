package com.kipti.bnb.content.cogwheel_chain.graph;

import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlock;
import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.registry.BnbBlocks;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CogwheelChain {

    List<CogwheelChainNode> nodes = new ArrayList<>();
    List<BlockPos> cogWheelPositions = new ArrayList<>();

    public CogwheelChain(CompoundTag tag) {
        read(tag);
    }

    public CogwheelChain(PartialCogwheelChain source) {
        this.nodes = CogwheelChainGeometryBuilder.buildFullChainFromPartial(source);
        this.cogWheelPositions = new ArrayList<>(source.visitedNodes.stream().map((e) -> e.pos().subtract(source.getFirstNode().pos())).toList());
    }

    public void write(CompoundTag tag) {
        for (int i = 0; i < nodes.size(); i++) {
            CogwheelChainNode node = nodes.get(i);
            CompoundTag nodeTag = new CompoundTag();
            node.write(nodeTag);
            tag.put("node_" + i, nodeTag);
        }
        tag.putInt("node_count", nodes.size());

        for (int i = 0; i < cogWheelPositions.size(); i++) {
            BlockPos pos = cogWheelPositions.get(i);
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            tag.put("cogwheel_pos_" + i, posTag);
        }
        tag.putInt("cogwheel_pos_count", cogWheelPositions.size());
    }

    public void read(CompoundTag tag) {
        nodes.clear();
        int nodeCount = tag.getInt("node_count");
        for (int i = 0; i < nodeCount; i++) {
            CompoundTag nodeTag = tag.getCompound("node_" + i);
            CogwheelChainNode node = CogwheelChainNode.read(nodeTag);
            nodes.add(node);
        }

        cogWheelPositions.clear();
        int cogWheelPosCount = tag.getInt("cogwheel_pos_count");
        for (int i = 0; i < cogWheelPosCount; i++) {
            CompoundTag posTag = tag.getCompound("cogwheel_pos_" + i);
            BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            cogWheelPositions.add(pos);
        }
    }

    protected CogwheelChainNode getLastNode() {
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
        for (BlockPos offset : cogWheelPositions) {
            BlockPos pos = worldPosition.offset(offset);
            removeChainCogwheelFromLevelIfPresent(level, pos);
        }
    }

    private void removeChainCogwheelFromLevelIfPresent(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        if (be instanceof CogwheelChainBlockEntity && (state.getBlock() instanceof CogwheelChainBlock cogwheelChainBlock)) {
            level.setBlockAndUpdate(pos, (cogwheelChainBlock.isLargeCog() ? AllBlocks.LARGE_COGWHEEL : AllBlocks.COGWHEEL).getDefaultState()
                .setValue(CogwheelChainBlock.AXIS, state.getValue(CogwheelChainBlock.AXIS)));
        }
    }

    public List<CogwheelChainNode> getNodes() {
        return nodes;
    }

}
