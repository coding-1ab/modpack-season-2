package org.antarcticgardens.newage.content.motors;

import org.antarcticgardens.newage.config.NewAgeConfig;

public enum MotorVariants{
    BASIC,
    ADVANCED,
    REINFORCED;

    public static long motorCapacity(MotorVariants variant) {
         long capacity = 0;
        switch (variant){
        case BASIC:
            capacity = NewAgeConfig.getCommon().basicMotorCapacity.get();
            break;
        case ADVANCED:
            capacity = NewAgeConfig.getCommon().advancedMotorCapacity.get();
            break;
        case REINFORCED:
            capacity = NewAgeConfig.getCommon().reinforcedMotorCapacity.get();
            break;
        }

        return capacity;
    }

    public static float motorSpeed(MotorVariants variant) {
        Double speed = 0.0;
        switch (variant){
        case BASIC:
            speed = NewAgeConfig.getCommon().basicMotorSpeed.get();
            break;
        case ADVANCED:
            speed = NewAgeConfig.getCommon().advancedMotorSpeed.get();
            break;
        case REINFORCED:
            speed = NewAgeConfig.getCommon().reinforcedMotorSpeed.get();
            break;
        }

        return speed.floatValue();
    }

    public static float motorStress(MotorVariants variant) {
        Double stress = 0.0;
        switch (variant){
        case BASIC:
            stress = NewAgeConfig.getCommon().basicMotorStress.get();
            break;
        case ADVANCED:
            stress = NewAgeConfig.getCommon().advancedMotorStress.get();
            break;
        case REINFORCED:
            stress = NewAgeConfig.getCommon().reinforcedMotorStress.get();
            break;
        }

        return stress.floatValue();
    }
}
