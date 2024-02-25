package org.antarcticgardens.cna.content.motor.extension.variants;

import org.antarcticgardens.cna.config.CNAConfig;

public class AdvancedMotorExtensionVariant implements IMotorExtensionVariant {
    @Override
    public float getMultiplier() {
        return CNAConfig.getCommon().advancedMotorExtensionMultiplier.get().floatValue();
    }

    @Override
    public long getExtraCapacity() {
        return CNAConfig.getCommon().advancedMotorExtensionExtraCapacity.get();
    }

    @Override
    public int getScrollStep() {
        return CNAConfig.getCommon().advancedMotorExtensionScrollStep.get();
    }
}
