package com.kipti.bnb;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber
public class BnbServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_FLYWHEEL_STORAGE = BUILDER
            .comment("Determines whether the flywheel storage functionality should be enabled, or if the flywheel should just be decorative.")
            .define("enable_flywheel_storage", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableFlywheelStorage;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC && event.getConfig().getType() == ModConfig.Type.SERVER) {
            enableFlywheelStorage = ENABLE_FLYWHEEL_STORAGE.get();
        }
    }

}
