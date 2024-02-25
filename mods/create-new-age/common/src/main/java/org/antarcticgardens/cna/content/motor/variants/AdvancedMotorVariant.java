package org.antarcticgardens.cna.content.motor.variants;

import org.antarcticgardens.cna.config.CNAConfig;

public class AdvancedMotorVariant implements IMotorVariant {
    @Override
    public long getMaxCapacity() {
        return CNAConfig.getCommon().advancedMotorCapacity.get();
    }

    @Override
    public float getSpeed() {
        return CNAConfig.getCommon().advancedMotorSpeed.get().floatValue();
    }

    @Override
    public float getStress() {
        return CNAConfig.getCommon().advancedMotorStress.get().floatValue();
    }
}
