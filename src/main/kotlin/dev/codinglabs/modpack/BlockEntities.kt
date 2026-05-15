package dev.codinglabs.modpack

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object BlockEntities {
    private val BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID)

    val VOID_ANCHOR: BlockEntityType<VoidAnchorBlockEntity> by BLOCK_ENTITY_TYPES.register("void_anchor", Supplier {
        BlockEntityType.Builder.of(::VoidAnchorBlockEntity, Blocks.VOID_ANCHOR).build(null)
    })

    fun register(bus: IEventBus) {
        BLOCK_ENTITY_TYPES.register(bus)
    }
}
