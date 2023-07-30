package org.antarcticgardens.newage.content.reactor.reactorrod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.content.heat.HeatBlockEntity;
import org.antarcticgardens.newage.content.reactor.NuclearUtil;
import org.jetbrains.annotations.Nullable;

public class ReactorRodBlockEntity extends BlockEntity implements HeatBlockEntity {
    public ReactorRodBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        working = blockState.getValue(ReactorRodBlock.ACTIVE);
    }

    int twoSeconds = 0;
    private boolean working;
    public void tick(BlockPos pos, Level world, BlockState state) {
        twoSeconds++;
        if (twoSeconds > 40) {
            working = state.getValue(ReactorRodBlock.ACTIVE);

            twoSeconds = 0;
            if (working) {
                NuclearUtil.createRadiation(8, world, pos);
            }
        }
        if (fuel > 0) {
            fuel--;
            if (!working) {
                world.setBlock(pos, state.setValue(ReactorRodBlock.ACTIVE, true), 3);
                working = true;
            }
            heat+=30;
            setChanged();
        } else {
            if (working) {
                world.setBlock(pos, state.setValue(ReactorRodBlock.ACTIVE, false), 3);
                working = false;
                setChanged();
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public float heat = 0;
    public int fuel = 0;

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = tag.getFloat("heat");
        fuel = tag.getInt("fuel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("heat", heat);
        tag.putInt("fuel", fuel);
    }


    @Override
    public float getHeat() {
        return heat;
    }

    @Override
    public void addHeat(float amount) {
        heat += amount;
        setChanged();
    }

    @Override
    public void setHeat(float amount) {
        heat = amount;
        setChanged();
    }

}
