package com.kipti.bnb;

import com.kipti.bnb.registry.*;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateBitsnBobs.MOD_ID)
public class CreateBitsnBobs {

    public static final String MOD_ID = "bits_n_bobs";
    public static final String NAME = "Create: Bits 'n' Bobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
        .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
        .setTooltipModifierFactory(item ->
            new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
        );

    public CreateBitsnBobs(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);

        BnbItems.register();
        BnbBlocks.register();
        BnbLangEntries.register();
        BnbCreativeTabs.register(modEventBus);
        BnbPartialModels.register();
        BnbBlockEntities.register();
        BnbTags.register();
        BnbDataComponents.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    public static ResourceLocation asResource(String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

}


