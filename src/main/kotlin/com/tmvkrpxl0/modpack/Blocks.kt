package com.tmvkrpxl0.modpack

import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object Blocks {
    private val BLOCKS = DeferredRegister.createBlocks(ModPackTweaks.ID)

    val ENDER_FIRE: EnderFire by BLOCKS.registerBlock(
        "ender_fire", ::EnderFire, BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_PURPLE)
        .replaceable()
        .noCollission()
        .instabreak()
        .lightLevel { _: BlockState? -> 10 }
        .sound(SoundType.WOOL)
        .pushReaction(PushReaction.DESTROY))

    val ENDER_FUEL: LiquidBlock by BLOCKS.registerBlock(
        "ender_fuel", {
        LiquidBlock(Fluids.ENDER_FUEL, it)
    }, BlockBehaviour.Properties.of()
        .replaceable()
        .noCollission()
        .randomTicks()
        .strength(100.0F)
        .lightLevel { 7 }
        .pushReaction(PushReaction.DESTROY)
        .noLootTable()
        .liquid()
        .sound(SoundType.EMPTY))

    val VOID_ANCHOR: VoidAnchorBlock by BLOCKS.registerBlock(
        "void_anchor", ::VoidAnchorBlock, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .strength(5.0f, 1200.0f) // 흑요석 폭발 저항
            .lightLevel { state -> if (state.getValue(VoidAnchorBlock.ACTIVE) && state.getValue(VoidAnchorBlock.CHARGES) > 0) 12 else 0 }
    )

    fun register(bus: IEventBus) {
        BLOCKS.register(bus)
    }

}
