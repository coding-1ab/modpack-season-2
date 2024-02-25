package org.antarcticgardens.cna.content.energising;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.cna.CNABlockEntityTypes;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.util.RunnableUtil;
import org.antarcticgardens.cna.util.StringFormatUtil;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.energy.SimpleEnergyStorage;

import java.util.List;

public class EnergiserBlockEntity extends KineticBlockEntity {
    private final SimpleEnergyStorage storage;

    public int tier;
    public float size = 0f;
    private EnergiserBehaviour energisingBehaviour;

    public EnergiserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        
        if (state.getBlock().equals(CNABlocks.BASIC_ENERGISER.get())) {
            tier = 1;
        } else if (state.getBlock().equals(CNABlocks.ADVANCED_ENERGISER.get())) {
            tier = 2;
        } else {
            tier = 3;
        }
        
        storage = new SimpleEnergyStorage(EnergiserBlock.getCapacity(tier))
                .onFinalCommit(RunnableUtil.createBlockEntityUpdater(this));
        
        EnergyStorage.registerForBlockEntity((blockEntity, direction) -> blockEntity.storage, CNABlockEntityTypes.ENERGISER.get());
        
        this.energisingBehaviour.tier = tier;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.putLong("Energy", storage.getStoredEnergy());
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        storage.setStoredEnergy(compound.getLong("Energy"));
        super.read(compound, clientPacket);
    }

    protected AABB createRenderBoundingBox() {
        var pos = new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return new AABB(pos.subtract(1, 3, 1), pos.add(1, 1, 1));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        energisingBehaviour = new EnergiserBehaviour(this);
        behaviours.add(energisingBehaviour);
    }

    public long lastCharged = -1;
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.create_new_age.energy_stats")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_stored")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.translate("tooltip.create_new_age.energy_storage", StringFormatUtil.formatLong(storage.getStoredEnergy()), StringFormatUtil.formatLong(storage.getCapacity()))
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 1);

        if (lastCharged != -1) {
            Lang.translate("tooltip.create_new_age.energy_usage")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            Lang.translate("tooltip.create_new_age.energy_per_tick", StringFormatUtil.formatLong(lastCharged))
                    .style(ChatFormatting.AQUA).forGoggles(tooltip, 1);
        }

        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
    
    public SimpleEnergyStorage getEnergyStorage() {
        return storage;
    }
}
