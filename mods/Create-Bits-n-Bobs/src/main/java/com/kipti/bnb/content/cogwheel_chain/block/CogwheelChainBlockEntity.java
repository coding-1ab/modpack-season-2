package com.kipti.bnb.content.cogwheel_chain.block;

import com.kipti.bnb.content.cogwheel_chain.graph.ChainPathCogwheelNode;
import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.girder_strut.IBlockEntityRelighter;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CogwheelChainBlockEntity extends SimpleKineticBlockEntity implements IBlockEntityRelighter {

    boolean isController = false;
    @Nullable CogwheelChain chain = null;
    @Nullable Vec3i controllerOffset = null;

    public CogwheelChainBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        isController = compound.getBoolean("IsController");
        if (compound.contains("ControllerOffsetX")) {
            controllerOffset = new Vec3i(
                compound.getInt("ControllerOffsetX"),
                compound.getInt("ControllerOffsetY"),
                compound.getInt("ControllerOffsetZ")
            );
        } else {
            controllerOffset = null;
        }

        if (isController) {
            if (chain != null && compound.contains("Chain")) {
                chain.read(compound.getCompound("Chain"));
            } else {
                chain = new CogwheelChain(compound.getCompound("Chain"));
            }
        } else {
            chain = null;
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean("IsController", isController);
        if (controllerOffset != null) {
            compound.putInt("ControllerOffsetX", controllerOffset.getX());
            compound.putInt("ControllerOffsetY", controllerOffset.getY());
            compound.putInt("ControllerOffsetZ", controllerOffset.getZ());
        }

        if (isController && chain != null) {
            CompoundTag chainTag = new CompoundTag();
            chain.write(chainTag);
            compound.put("Chain", chainTag);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (isController && chain != null) {
            chain.destroy(level, worldPosition);
        }
        if (!isController && controllerOffset != null) {
            BlockPos controllerPos = worldPosition.offset(controllerOffset);
            BlockEntity be = level.getBlockEntity(controllerPos);
            if (be instanceof CogwheelChainBlockEntity controllerBE) {
                controllerBE.chain.destroy(level, controllerPos);
            }
        }
    }

    public void setController(Vec3i offset) {
        this.isController = false;
        this.controllerOffset = offset;
    }

    public void setAsController(CogwheelChain cogwheelChain) {
        this.isController = true;
        this.chain = cogwheelChain;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(64);
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        List<BlockPos> toPropagate = new ArrayList<>(super.addPropagationLocations(block, state, neighbours));

        if (isController && chain != null) {
            addPropogationLocationsFromController(toPropagate, getBlockPos());
        } else {
//            if (controllerOffset != null)
//                toPropagate.add(getBlockPos().offset(controllerOffset));
            //Test putting child to child connections
            if (controllerOffset != null) {
                BlockPos controllerPos = worldPosition.offset(controllerOffset);
                BlockEntity be = level.getBlockEntity(controllerPos);
                if (be instanceof CogwheelChainBlockEntity controllerBE) {
                    controllerBE.addPropogationLocationsFromController(toPropagate, getBlockPos());
                }
            }
        }

        return toPropagate;
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
        if (connectedViaAxes && Math.abs(diff.get(target.getBlockState().getValue(CogwheelChainBlock.AXIS))) == 1)
            return 0;

        //Else, check if this is the same chain structure.
        if (target instanceof CogwheelChainBlockEntity chainTarget) {
            boolean isControlledBySame = this.isController &&
                chainTarget.controllerOffset != null &&
                chainTarget.controllerOffset.equals(this.getBlockPos().subtract(target.getBlockPos())) ||

                chainTarget.isController &&
                    this.controllerOffset != null &&
                    this.controllerOffset.equals(target.getBlockPos().subtract(this.getBlockPos())) ||

                chainTarget.controllerOffset != null &&
                    this.controllerOffset != null &&
                    this.controllerOffset.offset(this.getBlockPos()).equals(chainTarget.controllerOffset.offset(target.getBlockPos()));

            if (isControlledBySame) {
                int currentSide = this.getChainRotationDirection();
                int otherSide = chainTarget.getChainRotationDirection();
                return currentSide * otherSide;
            }
        }
        return 0;
    }

    public int getChainRotationDirection() {
        if (isController) {
            if (chain == null) return 0;

            ChainPathCogwheelNode controllerNode = chain.getNodeFromControllerOffset(new Vec3i(0, 0, 0));
            if (controllerNode == null) return 0;

            return controllerNode.side();
        }

        if (level == null || controllerOffset == null) return 0;

        BlockPos controllerPos = worldPosition.offset(controllerOffset);
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof CogwheelChainBlockEntity controllerBE) {

            CogwheelChain controllerChain = controllerBE.chain;
            if (controllerChain == null) return 0;

            ChainPathCogwheelNode nodeInChain = controllerChain.getNodeFromControllerOffset(controllerOffset);
            return nodeInChain == null ? 0 : nodeInChain.side();
        }
        return 0;
    }

    private void addPropogationLocationsFromController(List<BlockPos> toPropagate, BlockPos exclude) {
        assert chain != null;
        for (var cogwheelNode : chain.getChainPathCogwheelNodes()) {
            BlockPos cogwheelPos = worldPosition.offset(cogwheelNode.offsetFromStart());
            if (!toPropagate.contains(cogwheelPos) && !cogwheelPos.equals(exclude)) {
                toPropagate.add(cogwheelPos);
            }
        }
    }
}
