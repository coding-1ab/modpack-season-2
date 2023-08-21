package org.antarcticgardens.newage.content.heat.stirlingengine;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.config.NewAgeConfig;
import org.antarcticgardens.newage.content.heat.HeatBlockEntity;
import org.antarcticgardens.newage.tools.StringFormattingTool;

import java.util.List;

public class StirlingEngineBlockEntity extends GeneratingKineticBlockEntity implements HeatBlockEntity {

    LerpedFloat visualSpeed = LerpedFloat.linear();
    float angle;
    private float heat = 0;

    public StirlingEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putFloat("heat", heat);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.create_new_age.temperature", StringFormattingTool.formatFloat(heat))
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 1);

        Lang.translate("tooltip.create_new_age.using")
                .style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        Lang.translate("tooltip.create_new_age.temperature.ps", StringFormattingTool.formatFloat(speed * 6.25f)) // 6.25 is is 50/8
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 2);

        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (clientPacket)
            visualSpeed.chase(getGeneratedSpeed(), 1 / 64f, LerpedFloat.Chaser.EXP);

        heat = compound.getFloat("heat");

    }

    public float speed = 0;

    @Override
    public float getGeneratedSpeed() {
        return speed;
    }

    @Override
    public void tick() {
        super.tick();
        if (getLevel() == null)
            return;

        if (getLevel().getGameTime() % 20 == 0) {
            HeatBlockEntity.transferAround(this);
            if (getHeat() > 50) {
                if (getHeat() > 100) {
                    double multiplier = NewAgeConfig.getCommon().overheatingMultiplier.get();
                    if (getHeat() < 6000 * NewAgeConfig.getCommon().overheatingMultiplier.get()) {
                        setHeat(getHeat() - 100);
                        if (speed != 16) {
                            speed = 16;
                            updateGeneratedRotation();
                        }
                    } else if (multiplier > 0) {
                        getLevel().setBlock(getBlockPos(), Blocks.LAVA.defaultBlockState(), 3);
                    }
                } else {
                    setHeat(getHeat() - 50);
                    if (speed != 8) {
                        speed = 8;
                        updateGeneratedRotation();
                    }
                }
            } else {
                if (speed != 0) {
                    speed = 0;
                    updateGeneratedRotation();
                }
            }
        }

        if (!getLevel().isClientSide) {
            return;
        }

        float targetSpeed = getSpeed();
        visualSpeed.updateChaseTarget(targetSpeed);
        visualSpeed.tickChaser();
        angle += visualSpeed.getValue() * 3 / 10f;
        angle %= 360;
    }

    @Override
    public float getHeat() {
        return heat;
    }

    @Override
    public boolean canConnect(Direction from) {
        return from == Direction.UP;
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
