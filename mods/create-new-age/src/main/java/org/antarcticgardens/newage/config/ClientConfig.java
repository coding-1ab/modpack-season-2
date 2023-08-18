package org.antarcticgardens.newage.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public final ForgeConfigSpec.ConfigValue<Integer> wireSectionsPerMeter;
    public final ForgeConfigSpec.ConfigValue<Double> wireThickness;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        wireSectionsPerMeter = builder
                .comment(
                        "Choose how many wire sections are rendered in one meter (block).",
                        "Decreasing this value can theoretically improve performance"
                ).define("wireSectionsPerMeter", 10);

        wireThickness = builder
                .comment("...wire thickness...")
                .define("wireThickness", 0.03);
    }
}
