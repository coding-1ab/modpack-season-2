package org.antarcticgardens.newage.content.energiser;

import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EnergiserBlockEntity extends BasinOperatingBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer> {

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
        energy.setEnergy(energy.getStoredEnergy());
        super.read(compound, clientPacket);
    }

    public EnergiserBlockEntity(BlockPos pos, BlockState state, int level) {
        super(type, pos, state);
        energy = new WrappedBlockEnergyContainer(
                this, new SimpleEnergyContainer((long) (Math.pow(10, level) * 10000)));
        this.tier = level;
    }

    @Override
    protected boolean isRunning() {
        return false;
    }

    @Override
    protected void onBasinRemoved() {

    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new EnergiserBehaviour(this, (int)Math.pow(2, tier) * 2));
    }

    @Override
    protected <C extends net.minecraft.world.Container> boolean matchStaticFilters(Recipe<C> recipe) {
        return false;
    }
    @Override
    protected Object getRecipeCacheKey() {
        return null;
    }


    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        return energy;
    }
}
