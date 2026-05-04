package dev.codinglabs.modpack

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object Attachments {
    val ATTACHMENTS: DeferredRegister<AttachmentType<*>> = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ModPackTweaks.ID)

    val VOID_ANCHOR_POSITION: AttachmentType<BlockPos> by ATTACHMENTS.register("void_anchor_position", Supplier {
        AttachmentType.builder(Supplier { BlockPos.ZERO })
            .serialize(BlockPos.CODEC)
            .build()
    })

    fun register(bus: IEventBus) {
        ATTACHMENTS.register(bus)
    }
}

var ServerPlayer.voidAnchorPos: BlockPos?
    get() {
        return if (this.hasData(Attachments.VOID_ANCHOR_POSITION)) {
            this.getData(Attachments.VOID_ANCHOR_POSITION)
        } else null
    }
    set(value) {
        this.setData(Attachments.VOID_ANCHOR_POSITION, value!!)
    }
