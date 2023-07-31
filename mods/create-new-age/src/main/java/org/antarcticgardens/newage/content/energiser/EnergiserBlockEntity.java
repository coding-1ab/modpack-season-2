package org.antarcticgardens.newage.content.energiser;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EnergiserBlockEntity extends KineticBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer> {
    public WrappedBlockEnergyContainer energy;

    public int tier;
    public float size = 0f;

    public EnergiserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tier) {
        super(type, pos, state);
        energy = new WrappedBlockEnergyContainer(
                this, new SimpleEnergyContainer((long) (Math.pow(10, tier) * 10000)));
        this.tier = tier;
    }

    public static EnergiserBlockEntity newTier1(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new EnergiserBlockEntity(type, pos, state, 1);
    }
    public static EnergiserBlockEntity newTier2(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new EnergiserBlockEntity(type, pos, state, 2);
    }

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

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new EnergiserBehaviour(this, (int)Math.pow(2, tier*2)));
    }

    public long lastCharged = -1;
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.create_new_age.energy_stats")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_stored")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.translate("tooltip.create_new_age.energy_storage", energy.getStoredEnergy(), energy.getMaxCapacity())
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 1);

        if (lastCharged != -1) {
            Lang.translate("tooltip.create_new_age.energy_usage")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            Lang.translate("tooltip.create_new_age.energy_per_second", lastCharged * 20)
                    .style(ChatFormatting.AQUA).forGoggles(tooltip, 1);
        }

        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        return energy;
    }
}
