package org.antarcticgardens.newage.content.motors.extension;

import org.antarcticgardens.newage.config.NewAgeConfig;

public enum MotorExtensionVariants {
    BASIC,
    ADVANCED;

    // Multiplier, battery, scaler

    public static float extensionMultiplier(MotorExtensionVariants variant) {
        Double multiplier = 0.0;
        switch (variant){
        case BASIC:
            multiplier = NewAgeConfig.getCommon().basicMotorExtensionMultiplier.get();
            break;
        case ADVANCED:
            multiplier = NewAgeConfig.getCommon().advancedMotorExtensionMultiplier.get();
            break;
        }

        return multiplier.floatValue();
    }

    public static long extensionExtraCapacity(MotorExtensionVariants variant) {
        long capacity = 0;
        switch (variant){
        case BASIC:
            capacity = NewAgeConfig.getCommon().basicMotorExtensionExtraCapacity.get();
            break;
        case ADVANCED:
            capacity = NewAgeConfig.getCommon().advancedMotorExtensionExtraCapacity.get();
            break;
        }

        return capacity;
    }

    public static long extensionScaler(MotorExtensionVariants variant) {
        long scaler = 0;
        switch (variant){
        case BASIC:
            scaler = NewAgeConfig.getCommon().basicMotorExtensionScaler.get();
            break;
        case ADVANCED:
            scaler = NewAgeConfig.getCommon().advancedMotorExtensionScaler.get();
            break;
        }

        return scaler;
    }
}
