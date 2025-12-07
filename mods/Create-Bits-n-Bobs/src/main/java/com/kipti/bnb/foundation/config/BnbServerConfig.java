package com.kipti.bnb.foundation.config;

import com.google.common.collect.ImmutableMap;
import com.kipti.bnb.registry.BnbFeatureFlag;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BnbServerConfig extends ConfigBase {

    public final ConfigBool FLYWHEEL_STORAGE_CAPACITY = b(
            false,
            "flywheelBearingStorageCapability",
            "(This block is in development, and disabled in feature flags by default) Determines whether the flywheel bearing should be able to function as kinetic storage, setting this to false this means it will be purely decorative."
    );

    public final ConfigFloat FLYWHEEL_STORAGE_FACTOR = f(
            1, 0f, 100f,
            "flywheelStorageFactor",
            "(This block is in development, and disabled in feature flags by default) Multiplier for the kinetic storage capacity of flywheels mounted on flywheel bearings (if enabled). Values higher than one increase capacity. It is not recommended to set the value to 0, as you should disable the storage feature entirely."
    );

    public final ConfigFloat COGWHEEL_CHAIN_DRIVE_COST_FACTOR = f(
            1, 0f, 10f,
            "cogwheelChainDriveCostFactor",
            "Multiplier for the number of chains required for a cogwheel chain. Minimum cost is always 1 chain, unless this value is set to 0. Does not affect the number of chains returned by existing chain drives."
    );

    public final ConfigGroup FEATURE_FLAGS_GROUP = group(1, "featureFlags", "Feature flags to enable or disable certain features of the mod.");
    public final Map<BnbFeatureFlag, ConfigBool> FEATURE_FLAGS = createFeatureFlagConfigs();

    private Map<BnbFeatureFlag, ConfigBool> createFeatureFlagConfigs() {
        final HashMap<BnbFeatureFlag, ConfigBool> map = new HashMap<>();
        for (final BnbFeatureFlag featureFlag : BnbFeatureFlag.values()) {
            final ConfigBool configBool = b(
                    featureFlag.getDefaultState(),
                    enumToCamelCase(featureFlag.name().toLowerCase()),
                    featureFlag.getDescription()
            );
            map.put(featureFlag, configBool);
        }

        return ImmutableMap.copyOf(map);
    }

    private String enumToCamelCase(final String lowerCase) {
        final StringBuilder result = new StringBuilder();

        boolean capitalizeNext = false;
        for (final char c : lowerCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    @Override
    public @NotNull String getName() {
        return "server";
    }

    public boolean getFeatureFlagState(final BnbFeatureFlag featureFlagKey) {
        final ConfigBool configBool = FEATURE_FLAGS.get(featureFlagKey);
        return configBool != null && configBool.get();
    }

}
