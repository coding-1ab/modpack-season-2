package dev.propulsionteam.propulsionsimulated.content.heat;

public interface IHeatConsumer {
    boolean isActive();

    float getOperatingThreshold();
    
    float consumeHeat(float maxAvailable, float expectedHeatOutput, boolean simulate);
}

