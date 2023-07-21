package org.antarcticgardens.newage.content.energiser;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EnergiserBlockEntity extends KineticBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer> {

    public static BlockEntityType<EnergiserBlockEntity> type;

    public WrappedBlockEnergyContainer energy;

    public int tier;

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.putLong("energy", energy.getStoredEnergy());
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        energy.setEnergy(compound.getLong("energy"));
        super.read(compound, clientPacket);
    }

    public EnergiserBlockEntity(BlockPos pos, BlockState state, int level) {
        super(type, pos, state);
        energy = new WrappedBlockEnergyContainer(
                this, new SimpleEnergyContainer((long) (Math.pow(10, level) * 10000)));
        this.tier = level;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new EnergiserBehaviour(this, (int)Math.pow(2, tier)));
    }


    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        return energy;
    }
}
