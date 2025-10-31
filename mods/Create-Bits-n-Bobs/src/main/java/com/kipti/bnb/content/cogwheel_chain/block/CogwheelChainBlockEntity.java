package com.kipti.bnb.content.cogwheel_chain.block;

import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChain;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CogwheelChainBlockEntity extends SimpleKineticBlockEntity {

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
        if (compound.contains("ControllerOffset")) {
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

}
