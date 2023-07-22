package org.antarcticgardens.newage.content.heat.heatpipe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.content.heat.HeatBlockEntity;

public class HeatPipeBlockEntity extends BlockEntity implements HeatBlockEntity {
    public HeatPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public float heat = 0;

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = tag.getFloat("heat");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("heat", heat);
    }


    @Override
    public float getHeat() {
        return heat;
    }

    @Override
    public void addHeat(float amount) {
        heat += amount;
    }
}
