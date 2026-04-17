package org.example.com.tmvkrpxl0.modpack

import net.minecraft.world.item.BucketItem
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object Items {
    private val ITEMS = DeferredRegister.createItems(ModPackTweaks.ID)

    val ENDER_FUEL_BUCKET: BucketItem by ITEMS.registerItem("ender_fuel_bucket") {
        BucketItem(Fluids.ENDER_FUEL, it)
    }

    fun register(bus: IEventBus) {
        ITEMS.register(bus)
    }
}
