package com.tmvkrpxl0.modpack

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

@Suppress("unused")
object Items {
    private val ITEMS = DeferredRegister.createItems(ModPackTweaks.ID)
    private val TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, ModPackTweaks.ID)

    val ENDER_FUEL_BUCKET: BucketItem by ITEMS.registerItem("ender_fuel_bucket") {
        BucketItem(Fluids.ENDER_FUEL, it)
    }

    val SHULKER_SHELL_FRAGMENT: Item by ITEMS.registerSimpleItem("shulker_shell_fragment")

    val CREATIVE_TAB: CreativeModeTab by TABS.register("creative_tab", Supplier {
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.${ModPackTweaks.ID}.title"))
            .displayItems { param, output ->
                output.accept(ENDER_FUEL_BUCKET)
                output.accept(SHULKER_SHELL_FRAGMENT)
            }.build()
    })

    fun register(bus: IEventBus) {
        ITEMS.register(bus)
        TABS.register(bus)
    }
}
