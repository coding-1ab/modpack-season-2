package org.antarcticgardens.cna.content.motor.variants;

import org.antarcticgardens.cna.config.CNAConfig;

public class BasicMotorVariant implements IMotorVariant {
    @Override
    public long getMaxCapacity() {
        return CNAConfig.getCommon().basicMotorCapacity.get();
    }

    @Override
    public float getSpeed() {
        return CNAConfig.getCommon().basicMotorSpeed.get().floatValue();
    }

    @Override
    public float getStress() {
        return CNAConfig.getCommon().basicMotorStress.get().floatValue();
    }
}
