package org.antarcticgardens.newage.content.motors;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedBlockEnergyContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.newage.Configurations;
import org.antarcticgardens.newage.tools.StringFormattingTool;

import java.util.List;

public class MotorBlockEntity extends GeneratingKineticBlockEntity implements BotariumEnergyBlock<WrappedBlockEnergyContainer>, IHaveGoggleInformation {

    public boolean needsPower = false;
    public WrappedBlockEnergyContainer energy;
    private final float stressImpact;
    private final float maxSpeed;
    public MotorScrollValueBehaviour speedBehavior;
    public boolean powered = false;
    private float actualSpeed = 0;
    private float actualStress = 0;
    private long prvEnergy = -100000;
    private int energySpam = 0;

    private float speed = 0;
    private float stress = 0;

    public MotorBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3, long maxCapacity, float stressImpact, float maxSpeed) {
        super(arg, arg2, arg3);
        energy = new WrappedBlockEnergyContainer(this, new SimpleEnergyContainer(maxCapacity));
        this.stressImpact = stressImpact;
        this.maxSpeed = maxSpeed;
        speedBehavior.between((int) -maxSpeed, (int) maxSpeed);
    }

    public static BlockEntityBuilder.BlockEntityFactory<MotorBlockEntity> create(long capacity, float stressGenerated, float maxSpeed) {
        return (type, pos, state) -> new MotorBlockEntity(type, pos, state, capacity, stressGenerated, maxSpeed);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        speedBehavior = new MotorScrollValueBehaviour(Lang.translateDirect("kinetics.creative_motor.rotation_speed"), this, new MotorValueBox());
        speedBehavior.requiresWrench();
        speedBehavior.value = getDefaultSpeed();
        speedBehavior.withCallback(i -> this.updateGeneratedRotation());
        behaviours.add(speedBehavior);
    }

    static class MotorValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 12.5);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            Direction facing = state.getValue(CreativeMotorBlock.FACING);
            return super.getLocalOffset(state).add(Vec3.atLowerCornerOf(facing.getNormal())
                    .scale(-1 / 16f));
        }

        @Override
        public void rotate(BlockState state, PoseStack ms) {
            super.rotate(state, ms);
            Direction facing = state.getValue(CreativeMotorBlock.FACING);
            if (facing.getAxis() == Direction.Axis.Y)
                return;
            if (getSide() != Direction.UP)
                return;
            TransformStack.cast(ms)
                    .rotateZ(-AngleHelper.horizontalAngle(facing) + 180);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(CreativeMotorBlock.FACING);
            if (facing.getAxis() != Direction.Axis.Y && direction == Direction.DOWN)
                return false;
            return direction.getAxis() != facing.getAxis();
        }

    }


    public int getDefaultSpeed() {
        return 16;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        energy.setEnergy(compound.getLong("energy"));
        actualSpeed = compound.getFloat("aSpeed");
        needsPower = compound.getBoolean("needsPower");
        stress = compound.getFloat("lastGeneratedStress");
        speed = compound.getFloat("lastGeneratedSpeed");
        e = compound.getLong("eUse");
        super.read(compound, clientPacket);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.putLong("energy", energy.getStoredEnergy());
        compound.putFloat("aSpeed", actualSpeed);
        compound.putBoolean("needsPower", needsPower);
        compound.putFloat("lastGeneratedStress", stress);
        compound.putFloat("lastGeneratedSpeed", speed);
        compound.putFloat("eUse", e);
        super.write(compound, clientPacket);
    }

    @Override
    public float calculateStressApplied() {
        return 0;
    }

    private long e;

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.create_new_age.energy_stored")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_storage", StringFormattingTool.formatLong(energy.getStoredEnergy()), StringFormattingTool.formatLong(energy.getMaxCapacity()))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.translate("tooltip.create_new_age.using")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_per_second", StringFormattingTool.formatLong(e*20L))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        return true;
    }

    @Override
    public float calculateAddedStressCapacity() {
        this.lastCapacityProvided = stressImpact / actualSpeed;
        this.lastCapacityProvided = Float.isNaN(this.lastCapacityProvided) || Float.isInfinite(this.lastCapacityProvided) ? 0 : Math.abs(this.lastCapacityProvided);
        return this.lastCapacityProvided;
    }

    @Override
    public float getGeneratedSpeed() {
        return actualSpeed;
    }

    public void updateGeneratedRotation() {
        float speed = getGeneratedSpeed();
        float prevSpeed = this.speed;

        if (level == null || level.isClientSide)
            return;

        if (prevSpeed != speed) {
            if (!hasSource()) {
                IRotate.SpeedLevel levelBefore = IRotate.SpeedLevel.of(this.speed);
                IRotate.SpeedLevel levelafter = IRotate.SpeedLevel.of(speed);
                if (levelBefore != levelafter)
                    effects.queueRotationIndicators();
            }

            applyNewSpeed(prevSpeed, speed);
        }

        if (hasNetwork() && speed != 0) {
            KineticNetwork network = getOrCreateNetwork();
            network.updateCapacityFor(this, stress);
            notifyStressCapacityChange(calculateAddedStressCapacity());
            getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
            network.updateStress();
        }

        onSpeedChanged(prevSpeed);

        sendData();
    }

    @Override
    public void tick() {
        super.tick();

        float needed = (stressImpact) * Configurations.SU_TO_ENERGY;
        if (!level.isClientSide()) {
            e = energy.extractEnergy((long)Math.ceil(needed), false);
            if (e > 0) {
                actualSpeed = speedBehavior.value;
                actualStress = 256 * (e / needed) / actualSpeed;
            } else {
                actualSpeed = 0;
                actualStress = 0;
            }
            if (((actualSpeed != speed) || (actualStress != stress))) {
                updateGeneratedRotation();
                speed = actualSpeed;
                stress = actualStress;
            } else if (energy.getStoredEnergy() != prvEnergy && energySpam > 10) {
                this.sendData();
                prvEnergy = energy.getStoredEnergy();
                energySpam = 0;
            }
        }
        energySpam++;
    }

    @Override
    public WrappedBlockEnergyContainer getEnergyStorage() {
        return energy;
    }
}
