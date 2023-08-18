package org.antarcticgardens.newage.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public final ForgeConfigSpec.ConfigValue<Double> suToEnergy;
    public final ForgeConfigSpec.ConfigValue<Integer> maxRodsInDirection;
    public final ForgeConfigSpec.ConfigValue<Integer> maxCoils;

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
    }
}
