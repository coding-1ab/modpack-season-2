package org.antarcticgardens.newage.content.generation.carbonbrushes;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.Lang;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.ExtractOnlyEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.content.generation.generatorcoil.GeneratorCoilBlockEntity;

import java.util.List;

public class CarbonBrushesBlockEntity extends KineticBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer>, IHaveGoggleInformation {
    public static int MAX_COILS = 8;

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
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        energyContainer.setEnergy(compound.getLong("energy"));
        super.read(compound, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.create_new_age.energy_stats")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_stored")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_storage", energyContainer.getStoredEnergy(), energyContainer.getMaxCapacity())
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.translate("tooltip.create_new_age.energy_output")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_per_second", lastOutput)
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public void lazyTick() {
        Direction.Axis axis = getBlockState().getValue(DirectionalKineticBlock.FACING).getAxis();

        int coilAmount = 0;
        lastOutput = 0;

        for (int i = 1; i <= MAX_COILS; i++) {
            if (coilAmount >= MAX_COILS)
                break;

            if (processBlockEntityAt(worldPosition.relative(axis, i), coilAmount))
                coilAmount++;

            if (processBlockEntityAt(worldPosition.relative(axis, -i), coilAmount))
                coilAmount++;
        }
    }

    private boolean processBlockEntityAt(BlockPos pos, int coilAmount) {
        if (coilAmount >= MAX_COILS)
            return false;

        if (level.getBlockEntity(pos) instanceof GeneratorCoilBlockEntity coil) {
            int energy = coil.takeGeneratedEnergy();
            lastOutput += energy;
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
