package com.kipti.bnb.content.flywheel_bearing.mechanics;

import com.kipti.bnb.BnbServerConfig;
import com.kipti.bnb.content.flywheel_bearing.FlywheelBearingBlockEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class FlywheelMovementMechanics {

    /**
     * Should be a config value
     * Converts literal kinetic energy in joules to stress units
     */
    public static float STRESS_UNITS_PER_KE_JOULE = 1 / 512f;

    /**
     * Should be a config value too
     */
    public static float TRANSFER_CAPACITY_PER_ANGULAR_MASS = 5;

    public float maxAngularVelocity = (360f * 64) / (60 * 20);
    public float angularVelocity = 0;
    public float prevAngle = 0;
    public float angle = 0;
    public float angularMass = 1f;

    /**
     * Writes the fields directly into the tag, be sure not to conflict
     */
    public void writeAdditional(final CompoundTag compound) {
//        compound.putFloat("MaxAngularVelocity", maxAngularVelocity);
        compound.putFloat("AngularVelocity", angularVelocity);
        compound.putFloat("PrevAngle", prevAngle);
        compound.putFloat("Angle", angle);
        compound.putFloat("AngularMass", angularMass);
    }

    /**
     * Reads the fields directly from the tag, be sure not to conflict
     */
    public void readAdditional(final CompoundTag compound) {
//        if (compound.contains("MaxAngularVelocity"))
//            maxAngularVelocity = compound.getFloat("MaxAngularVelocity");
        if (compound.contains("AngularVelocity"))
            angularVelocity = compound.getFloat("AngularVelocity");

        if (BnbServerConfig.enableFlywheelStorage) {
            if (angularVelocity < 0)
                angularVelocity = 0; //Flywheel storage only works with positive angular velocity
        }

        if (compound.contains("PrevAngle"))
            prevAngle = compound.getFloat("PrevAngle");
        if (compound.contains("Angle"))
            angle = compound.getFloat("Angle");
        if (compound.contains("AngularMass"))
            angularMass = compound.getFloat("AngularMass");
    }

    public void tickForStorageBehaviour(final FlywheelBearingBlockEntity be) {
        prevAngle = angle;

        final boolean canReceiveStressBefore = canReceiveStress();
        final boolean canProvideStressBefore = canProvideStress();

        STRESS_UNITS_PER_KE_JOULE = 8f;

        final float maxTransferCapacity = getMaxTransferCapacity();
        final float availableExcessKineticStrength = Math.clamp(be.getFlywheelStressDelta(), -maxTransferCapacity, maxTransferCapacity);
        final float kejEnergyChange = availableExcessKineticStrength / STRESS_UNITS_PER_KE_JOULE;
        final float currentKejEnergy = 0.5f * angularMass * angularVelocity * angularVelocity;
        final float newKejEnergy = Math.max(currentKejEnergy + kejEnergyChange, 0);

        angularVelocity = angularMass == 0 ? 0 : (float) Math.sqrt((2f * newKejEnergy) / angularMass);

        if (angularVelocity > maxAngularVelocity) {
            angularVelocity = maxAngularVelocity;
        }

        angle += angularVelocity;

        final boolean canNowProvideStress = canProvideStress();
        if (canProvideStressBefore != canNowProvideStress) {
            be.updateGeneratedRotation();
            be.updateFlywheelStressesInNetwork();
            if (!canNowProvideStress) {
                angularVelocity = 0;
                //TODO: particle effect
            }
        }

        if (canReceiveStressBefore != canReceiveStress()) {
            be.updateFlywheelStressesInNetwork();
        }
    }

    public void assemble(final FlywheelBearingBlockEntity be, BearingContraption contraption) {
        angularMass = 0f;
        final Vec3 axis = Vec3.atLowerCornerOf(contraption.getFacing().getNormal());
        final Vec3 anchor = Vec3.atCenterOf(BlockPos.ZERO);

        for (final Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> block : contraption.getBlocks().entrySet()) {
            final float angularDistance = Math.max((float) block.getKey().getCenter().subtract(anchor).cross(axis).length(), 1 / 16f);
            final float mass = getMassOfBlock(block.getValue().state());
            angularMass += mass * angularDistance * angularDistance;
        }
        be.updateFlywheelStressesInNetwork();
    }

    public void tick(FlywheelBearingBlockEntity flywheelBearingBlockEntity) {
        prevAngle = angle;
        final float targetAngularVelocity = flywheelBearingBlockEntity.getSpeed() * 360 / (20f * 60f);
        final float reactivity = Math.clamp(1f / angularMass, 0.005f, 1f);
        angularVelocity = targetAngularVelocity * reactivity + angularVelocity * (1 - reactivity);
        angle += angularVelocity;
    }

    private float getMassOfBlock(BlockState state) {
        return 1f;
    }

    public boolean canReceiveStress() {
        return angularVelocity < maxAngularVelocity;
    }

    public float getStoredStressTicks() {
        final float kejEnergy = 0.5f * angularMass * angularVelocity * angularVelocity;
        return kejEnergy * STRESS_UNITS_PER_KE_JOULE;
    }

    public boolean canProvideStress() {
        return getStoredStressTicks() >= getMaxTransferCapacity();
    }

    public float getMaxTransferCapacity() {
        return Math.max(1, angularMass * TRANSFER_CAPACITY_PER_ANGULAR_MASS);
    }

    public float getFlywheelStressCapacity() {
        return canReceiveStress() ? getMaxTransferCapacity() : 0;
    }

    public float getMaxAngularVelocity() {
        return maxAngularVelocity;
    }

}
