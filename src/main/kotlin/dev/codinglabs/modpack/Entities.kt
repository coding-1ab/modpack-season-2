package dev.codinglabs.modpack

import dev.codinglabs.modpack.rapier_entity.dragon.EnderDragonPrototype
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Function

object Entities {
    val ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ID)

    val ENDER_DRAGON_PROTOTYPE: EntityType<EnderDragonPrototype> by ENTITIES.register(
        "ender_dragon_prototype",
        Function { name ->
            EntityType.Builder.of(::EnderDragonPrototype, MobCategory.MONSTER)
                .fireImmune()
                .sized(16.0F, 8.0F)
                .passengerAttachments(3.0F)
                .clientTrackingRange(10)
                .build(name.path)
        })

    fun register(bus: IEventBus) {
        ENTITIES.register(bus)
    }
}
