package org.antarcticgardens.cna.content.motor.extension.variants;

import org.antarcticgardens.cna.config.CNAConfig;

public class BasicMotorExtensionVariant implements IMotorExtensionVariant {
    @Override
    public float getMultiplier() {
        return CNAConfig.getCommon().basicMotorExtensionMultiplier.get().floatValue();
    }

    @Override
    public long getExtraCapacity() {
        return CNAConfig.getCommon().basicMotorExtensionExtraCapacity.get();
    }

    @Override
    public int getScrollStep() {
        return CNAConfig.getCommon().basicMotorExtensionScrollStep.get();
    }
}
