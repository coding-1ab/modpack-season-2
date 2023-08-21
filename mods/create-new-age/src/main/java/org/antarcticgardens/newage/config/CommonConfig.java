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

    public CommonConfig(ForgeConfigSpec.Builder builder) {
        suToEnergy = builder
                .comment(
                        "Responsible for how much energy is generated per 1 stress unit in a tick",
                        "Default value is supposed to be compatible with default configuration of Create: Crafts and Additions"
                ).define("suToEnergy", 0.029296875);

        maxCoils = builder
                .comment("How many coils can the carbon brushes collect energy from")
                .define("maxCoils", 8);

        maxRodsInDirection = builder
                .comment("How many reactor rods can a fuel inserter or a heat vent have in a single direction")
                .define("maxRodsInDirection", 32);

        conductivityMultiplier = builder
                .comment("Multiplier of wire conductivity")
                .define("conductivityMultiplier", 1.0);

        maxPathfindingDepth = builder
                .comment("Maximum depth of network pathfinding")
                .define("maxPathfindingDepth", 32);

        energiserSpeedMultiplier = builder
                .comment("Multiplier of energising speed")
                .define("energiserSpeedMultiplier", 1.0);

        overheatingMultiplier = builder
                .comment("Multiplier for the temperature at which components overheat. Set to less than 0 to disable overheating.")
                .define("overheatingMultiplier", 1.0);

    }
}
