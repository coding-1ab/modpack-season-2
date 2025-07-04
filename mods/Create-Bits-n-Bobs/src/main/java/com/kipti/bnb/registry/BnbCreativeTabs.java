package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.AllCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

public class BnbCreativeTabs {


    private static final DeferredRegister<CreativeModeTab> REGISTER =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateBitsnBobs.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE_CREATIVE_TAB = REGISTER.register("based",
        () -> CreativeModeTab.builder()
            .title(Component.literal(CreateBitsnBobs.NAME))
            .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
            .icon(BnbBlocks.LIGHTBULB::asStack)
            .displayItems((parameters, output) -> {
                for (RegistryEntry<Item, Item> item : CreateBitsnBobs.REGISTRATE.getAll(Registries.ITEM)) {
                    output.accept(item.get());
                }
            }).build());

    @ApiStatus.Internal
    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

}
