package dev.codinglabs.modpack

import net.minecraft.core.BlockPos
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object Attachments {
    val ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ModPackTweaks.ID)

    val VOID_ANCHOR_POSITION by ATTACHMENTS.register("void_anchor_position", Supplier {
        AttachmentType.builder(Supplier { BlockPos.ZERO })
            .serialize(BlockPos.CODEC)
            .build()
    })

    fun register(bus: IEventBus) {
        ATTACHMENTS.register(bus)
    }
}
