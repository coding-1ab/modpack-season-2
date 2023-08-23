package org.antarcticgardens.newage.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public final ForgeConfigSpec.ConfigValue<Double> suToEnergy;
    public final ForgeConfigSpec.ConfigValue<Integer> maxRodsInDirection;
    public final ForgeConfigSpec.ConfigValue<Integer> maxCoils;
    public final ForgeConfigSpec.ConfigValue<Double> conductivityMultiplier;
    public final ForgeConfigSpec.ConfigValue<Integer> maxPathfindingDepth;
    public final ForgeConfigSpec.ConfigValue<Double> energiserSpeedMultiplier;

    public final ForgeConfigSpec.ConfigValue<Double> overheatingMultiplier;

    public final ForgeConfigSpec.ConfigValue<Double> nuclearReactorRodHeat;

    public final ForgeConfigSpec.ConfigValue<Double> nuclearReactorRodHeatLoss;

    public final ForgeConfigSpec.ConfigValue<Double> solarPanelHeatMultiplier;



    public CommonConfig(ForgeConfigSpec.Builder builder) {
        suToEnergy = builder
                .comment(
                        "Responsible for how much energy is generated per 1 stress unit in a tick",
                        "Default value is supposed to be compatible with default configuration of Create: Crafts and Additions"
                ).defineInRange("suToEnergy", 0.029296875, 0, Double.MAX_VALUE);

        maxCoils = builder
                .comment("How many coils can the carbon brushes collect energy from")
                .defineInRange("maxCoils", 8, 0, Integer.MAX_VALUE);

        maxRodsInDirection = builder
                .comment("How many reactor rods can a fuel inserter or a heat vent have in a single direction")
                .defineInRange("maxRodsInDirection", 32, 0, Integer.MAX_VALUE);

        conductivityMultiplier = builder
                .comment("Multiplier of wire conductivity")
                .defineInRange("conductivityMultiplier", 1.0, 0, Double.MAX_VALUE);

        maxPathfindingDepth = builder
                .comment("Maximum depth of network pathfinding")
                .defineInRange("maxPathfindingDepth", 32, 1, Integer.MAX_VALUE);

        energiserSpeedMultiplier = builder
                .comment("Multiplier of energising speed")
                .defineInRange("energiserSpeedMultiplier", 1.0, 0, Double.MAX_VALUE);

        overheatingMultiplier = builder
                .comment("Multiplier for the temperature at which components overheat. Set to -1 to disable overheating.")
                .defineInRange("overheatingMultiplier", 1.0, -1, Double.MAX_VALUE);

        nuclearReactorRodHeat = builder
                .comment("How much heat per tick a nuclear reactor rod generate.")
                .defineInRange("ReactorRodHeat", 30.0, 0, Double.MAX_VALUE);

        nuclearReactorRodHeatLoss = builder
                .comment("How much heat per tick a nuclear reactor rod looses above the 16000*overheatingMultiplier.")
                .defineInRange("ReactorRodHeatLoss", 25.0, 0, Double.MAX_VALUE);

        solarPanelHeatMultiplier = builder
                .comment("Multiplier for how much heat solar panels output.")
                .defineInRange("solarPanelHeatMultiplier", 1.0, 0, Double.MAX_VALUE);

    }
}
