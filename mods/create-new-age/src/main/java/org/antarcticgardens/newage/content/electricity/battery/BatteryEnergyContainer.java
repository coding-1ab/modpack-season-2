package org.antarcticgardens.newage.content.electricity.battery;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergySnapshot;
import earth.terrarium.botarium.util.Updatable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.newage.NewAgeTags;

public class BatteryEnergyContainer implements EnergyContainer, Updatable<BlockEntity> {
    public static final long BUCKET = 81000;
    public static final String TAG = "CreateNewAge";
    
    private FluidTankBlockEntity tank = null;
    private long clientEnergy = 0;
    
    public void setTank(FluidTankBlockEntity tank) {
        this.tank = tank;
        setEnergy(getStoredEnergy());
    }
    
    public FluidTankBlockEntity getTank() {
        return tank;
    }

    @Override
    public long insertEnergy(long maxAmount, boolean simulate) {
        long inserted = (long) Mth.clamp(maxAmount, 0, getMaxCapacity() - getStoredEnergy());
        if (simulate) return inserted;
        this.setEnergy(getStoredEnergy() + inserted);
        return inserted;
    }

    @Override
    public long extractEnergy(long maxAmount, boolean simulate) {
        long extracted = (long) Mth.clamp(maxAmount, 0, getStoredEnergy());
        if (simulate) return extracted;
        this.setEnergy(getStoredEnergy() - extracted);
        return extracted;
    }

    @Override
    public void setEnergy(long energy) {
        if (isElectrolyteBattery()) {
            energy = Math.min(energy, getMaxCapacity());
            CompoundTag nbt = tank.getTankInventory().getFluid().getOrCreateTag();
            long mc = getMaxCapacity();
            nbt.putDouble("Energy", (double) energy / mc);
            tank.getTankInventory().getFluid().setTag(nbt);
            clientEnergy = energy;
        }
    }

    @Override
    public long getStoredEnergy() {
        if (!isElectrolyteBattery())
            return 0;
        
        CompoundTag nbt = tank.getTankInventory().getFluid().getOrCreateTag();
        return (long) (nbt.getDouble("Energy") * getMaxCapacity());
    }

    @Override
    public long getMaxCapacity() {
        if (!isElectrolyteBattery())
            return 0;
        
        return tank.getTankInventory().getFluidAmount() / BUCKET * 50000;
    }
    
    public long getClientEnergy() {
        return clientEnergy;
    }
    
    private boolean isElectrolyteBattery() {
        return tank != null && tank.getTankInventory().getFluid().getFluid().is(NewAgeTags.ELECTROLYTE_TAG);
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
    public CompoundTag serialize(CompoundTag root) {
        CompoundTag tag = root.getCompound(TAG);
        tag.putLong("Energy", clientEnergy);
        root.put(TAG, tag);
        return root;
    }

    @Override
    public void deserialize(CompoundTag root) {
        CompoundTag tag = root.getCompound(TAG);
        clientEnergy = tag.getLong("Energy");
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
        EnergySnapshot es = new SimpleEnergySnapshot(this);
        return es;
    }

    @Override
    public void clearContent() {
        setEnergy(0);
    }

    @Override
    public void update(BlockEntity object) {
        if (object.getLevel() != null && !object.getLevel().isClientSide())
            clientEnergy = getStoredEnergy();
        
        object.setChanged();
        object.getLevel().sendBlockUpdated(object.getBlockPos(), object.getBlockState(), object.getBlockState(), Block.UPDATE_ALL);
    }
}
