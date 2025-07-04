package com.kipti.bnb;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue FORCE_DISABLE_VEIL = BUILDER
            .comment("Force veils dynamic lighting to be disabled nonfunctioningfornowplsfix")
            .define("force_disable_veil", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean forceDisableVeil;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            forceDisableVeil = FORCE_DISABLE_VEIL.get();
        }
    }

}
