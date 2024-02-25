package org.antarcticgardens.cna.content.motor.variants;

import org.antarcticgardens.cna.config.CNAConfig;

public class ReinforcedMotorVariant implements IMotorVariant {
    @Override
    public long getMaxCapacity() {
        return CNAConfig.getCommon().reinforcedMotorCapacity.get();
    }

    @Override
    public float getSpeed() {
        return CNAConfig.getCommon().reinforcedMotorSpeed.get().floatValue();
    }

    @Override
    public float getStress() {
        return CNAConfig.getCommon().reinforcedMotorStress.get().floatValue();
    }
}
