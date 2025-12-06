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
            "experimentalFlywheelBearingStorageCapability",
            "Determines whether the flywheel bearing (block is experimental and is disabled by default) should be able to function as kinetic storage, setting this to false this means it will be purely decorative."
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
