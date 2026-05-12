package dev.codinglabs.modpack

import com.mojang.serialization.Codec
import dev.codinglabs.modpack.Attachments.LAST_TELEPORT_TICK
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier
import kotlin.Long
import kotlin.jvm.optionals.getOrNull

object Attachments {
    val ATTACHMENTS: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ModPackTweaks.ID)

    val VOID_ANCHOR_POSITION: AttachmentType<BlockPos> by ATTACHMENTS.register("void_anchor_position", Supplier {
        AttachmentType.builder(Supplier { BlockPos.ZERO })
            .serialize(BlockPos.CODEC)
            .build()
    })

    val TELEPORT_REQUEST: AttachmentType<Commands.TeleportRequest> by ATTACHMENTS.register("teleport_request", Supplier {
        AttachmentType.builder(Supplier { Commands.TeleportRequest(Commands.TeleportType.Spawn, 0) })
            .serialize(Commands.TeleportRequest.CODEC)
            .build()
    })

    val LAST_TELEPORT_TICK: AttachmentType<Long> by ATTACHMENTS.register("last_teleport_tick", Supplier {
        AttachmentType.builder( Supplier { 0L })
            .serialize(Codec.LONG)
            .build()
    })

    fun register(bus: IEventBus) {
        ATTACHMENTS.register(bus)
    }
}

var ServerPlayer.lastTeleportTick: Long
    get() {
        return this.getData(LAST_TELEPORT_TICK)
    }
    set(value) {
        this.setData(LAST_TELEPORT_TICK, value)
    }

var ServerPlayer.teleportRequest: Commands.TeleportRequest?
    get() {
        return this.getExistingData(Attachments.TELEPORT_REQUEST).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setData(Attachments.TELEPORT_REQUEST, value)
        } else {
            this.removeData(Attachments.TELEPORT_REQUEST)
        }
    }

var ServerPlayer.voidAnchorPos: BlockPos?
    get() {
        return this.getExistingData(Attachments.VOID_ANCHOR_POSITION).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setData(Attachments.VOID_ANCHOR_POSITION, value)
        } else {
            this.removeData(Attachments.VOID_ANCHOR_POSITION)
        }
    }
