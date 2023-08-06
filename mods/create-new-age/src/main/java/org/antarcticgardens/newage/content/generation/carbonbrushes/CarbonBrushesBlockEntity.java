package org.antarcticgardens.newage.content.generation.carbonbrushes;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.Lang;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.ExtractOnlyEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import earth.terrarium.botarium.common.energy.util.EnergyHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.Configurations;
import org.antarcticgardens.newage.content.generation.generatorcoil.GeneratorCoilBlockEntity;
import org.antarcticgardens.newage.tools.StringFormattingTool;

import java.util.List;

public class CarbonBrushesBlockEntity extends KineticBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer>, IHaveGoggleInformation {
    private final WrappedBlockEnergyContainer energyContainer;

    private int lastOutput = 0;

    public CarbonBrushesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        energyContainer = new WrappedBlockEnergyContainer(this, new ExtractOnlyEnergyContainer(25000));

        setLazyTickRate(20);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.putLong("energy", energyContainer.getStoredEnergy());
        compound.putInt("lastOutput", lastOutput);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        energyContainer.setEnergy(compound.getLong("energy"));
        lastOutput = compound.getInt("lastOutput");
        super.read(compound, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.create_new_age.energy_stats")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_stored")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_storage", StringFormattingTool.formatLong(energyContainer.getStoredEnergy()), StringFormattingTool.formatLong(energyContainer.getMaxCapacity()))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.translate("tooltip.create_new_age.energy_output")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_per_second", StringFormattingTool.formatLong(lastOutput*20L))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        return true;
    }

    private int syncOut = 0;

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        Direction.Axis axis = getBlockState().getValue(DirectionalKineticBlock.FACING).getAxis();

        int coilAmount = 0;
        lastOutput = 0;

        for (int i = 1; i <= Configurations.MAX_COILS; i++) {
            if (coilAmount >= Configurations.MAX_COILS)
                break;

            if (processBlockEntityAt(worldPosition.relative(axis, i), coilAmount))
                coilAmount++;

            if (processBlockEntityAt(worldPosition.relative(axis, -i), coilAmount))
                coilAmount++;
        }

        EnergyHooks.distributeEnergyNearby(this);
    }

    @Override
    public void lazyTick() {
        if (level == null || level.isClientSide) return;
        if (syncOut > 0) {
            syncOut = 0;
            setChanged();
            sendData();
        }
    }

    private boolean processBlockEntityAt(BlockPos pos, int coilAmount) {
        if (coilAmount >= Configurations.MAX_COILS)
            return false;

        if (level.getBlockEntity(pos) instanceof GeneratorCoilBlockEntity coil) {
            int energy = coil.takeGeneratedEnergy();
            lastOutput += energy;
            syncOut += energy;
            energyContainer.internalInsert(energy, false);
            return true;
        }

        return false;
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        return energyContainer;
    }
}
