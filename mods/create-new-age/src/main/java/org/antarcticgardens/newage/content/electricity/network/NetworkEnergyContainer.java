package org.antarcticgardens.newage.content.electricity.network;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergySnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.newage.NewAgeBlocks;
import org.antarcticgardens.newage.content.electricity.connector.ElectricalConnectorBlockEntity;

public class NetworkEnergyContainer implements EnergyContainer {
    private final ElectricalConnectorBlockEntity connector;
    private final ElectricalNetwork network;

    public NetworkEnergyContainer(ElectricalConnectorBlockEntity connector, ElectricalNetwork network) {
        this.connector = connector;
        this.network = network;
    }

    @Override
    public long insertEnergy(long maxAmount, boolean simulate) {
        if (connector.getLevel() != null) {
            BlockEntity entity = connector.getLevel()
                    .getBlockEntity(NewAgeBlocks.ELECTRICAL_CONNECTOR.get().getSupportingBlockPos(connector));

            if (entity instanceof BotariumEnergyBlock<?> energyBlock) {
                long canExtract = energyBlock.getEnergyStorage().extractEnergy(Long.MAX_VALUE, true);
                long inserted = network.insert(connector, canExtract, false);
                energyBlock.getEnergyStorage().extractEnergy(inserted, false);
            }
        }

        return 0;
    }

    @Override
    public long extractEnergy(long maxAmount, boolean simulate) {
        return 0;
    }

    @Override
    public long getStoredEnergy() {
        return 0;
    }

    @Override
    public long getMaxCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long maxInsert() {
        return Long.MAX_VALUE;
    }

    @Override
    public long maxExtract() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    @Override
    public EnergySnapshot createSnapshot() {
        return new SimpleEnergySnapshot(this);
    }

    @Override
    public void setEnergy(long energy) {

    }

    @Override
    public void clearContent() {

    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return new CompoundTag();
    }
}
