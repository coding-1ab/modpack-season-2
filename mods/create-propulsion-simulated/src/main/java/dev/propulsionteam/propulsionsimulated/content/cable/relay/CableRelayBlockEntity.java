package dev.propulsionteam.propulsionsimulated.content.cable.relay;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class CableRelayBlockEntity extends SmartBlockEntity {
    public static final int CAPACITY = 10_000;
    private int energy;

    private final IEnergyStorage storage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) return 0;
            int accepted = Math.min(maxReceive, CAPACITY - energy);
            if (!simulate && accepted > 0) {
                energy += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (maxExtract <= 0) return 0;
            int extracted = Math.min(maxExtract, energy);
            if (!simulate && extracted > 0) {
                energy -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override public int getEnergyStored() { return energy; }
        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    };

    public CableRelayBlockEntity(BlockPos pos, BlockState blockState) {
        super(PropulsionBlockEntities.CABLE_RELAY_BLOCK_ENTITY.get(), pos, blockState);
    }

    public IEnergyStorage getEnergyHandler(Direction side) {
        return storage;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("Energy", energy);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        energy = Math.clamp(tag.getInt("Energy"), 0, CAPACITY);
        super.read(tag, registries, clientPacket);
    }
}
