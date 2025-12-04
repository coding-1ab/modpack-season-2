package com.kipti.bnb.foundation.config;

import com.google.common.collect.ImmutableMap;
import com.kipti.bnb.registry.BnbFeatureFlag;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.simibubi.create.foundation.utility.StringHelper.snakeCaseToCamelCase;

public class BnbServerConfig extends ConfigBase {

    public final ConfigBool FLYWHEEL_STORAGE_CAPACITY = b(
            false,
            "flywheelStorageCapacity",
            "Determines whether the flywheel storage functionality should be enabled, or if the flywheel should be purely decorative."
    );

    public final Map<BnbFeatureFlag, ConfigBool> FEATURE_FLAGS = createFeatureFlagConfigs();

    private Map<BnbFeatureFlag, ConfigBool> createFeatureFlagConfigs() {
        final HashMap<BnbFeatureFlag, ConfigBool> map = new HashMap<>();

        for (final BnbFeatureFlag featureFlag : BnbFeatureFlag.values()) {
            final ConfigBool configBool = b(
                    true,
                    snakeCaseToCamelCase(featureFlag.name().toLowerCase()), //Oh my goodness i typed this function name and it exists thats like awesome
                    featureFlag.getDescription()
            );
            map.put(featureFlag, configBool);
        }

        return ImmutableMap.copyOf(map);
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
