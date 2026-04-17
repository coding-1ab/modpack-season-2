package org.example.com.tmvkrpxl0.modpack

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Rarity
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.fluids.BaseFlowingFluid
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object Fluids {
    private val FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, ModPackTweaks.ID)
    private val FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, ModPackTweaks.ID)

    val ENDER_FUEL_TYPE: FluidType by FLUID_TYPES.register("ender_fuel", Supplier {
        FluidType(
            FluidType.Properties.create()
                .lightLevel(7)
                .viscosity(300)
                .canSwim(false)
                .rarity(Rarity.UNCOMMON)
        )
    })
    private val enderFuelProperties = BaseFlowingFluid.Properties(
        { ENDER_FUEL_TYPE },
        { ENDER_FUEL },
        { ENDER_FUEL_FLOWING }
    ).block { Blocks.ENDER_FUEL }
        .bucket { Items.ENDER_FUEL_BUCKET }
        .explosionResistance(100F)
        .levelDecreasePerBlock(4)
        .tickRate(30)

    val ENDER_FUEL: EnderFuel.Source by FLUIDS.register("ender_fuel", Supplier {
        EnderFuel.Source(enderFuelProperties)
    })

    val ENDER_FUEL_FLOWING: EnderFuel.Flowing by FLUIDS.register("flowing_ender_fuel", Supplier {
        EnderFuel.Flowing(enderFuelProperties)
    })

    fun register(bus: IEventBus) {
        FLUIDS.register(bus)
        FLUID_TYPES.register(bus)
    }
}
