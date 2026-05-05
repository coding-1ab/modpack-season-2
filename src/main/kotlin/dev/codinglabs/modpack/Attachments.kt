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
        // 데이터가 없거나, 기본값(ZERO)인 경우 null로 취급하고 싶다면 아래와 같이 작성
        return if (this.hasData(Attachments.VOID_ANCHOR_POSITION)) {
            this.getData(Attachments.VOID_ANCHOR_POSITION)
        } else null
    }
    set(value) {
        if (value == null) {
            // null을 대입하려고 하면 데이터를 아예 삭제합니다.
            this.removeData(Attachments.VOID_ANCHOR_POSITION)
        } else {
            // 값이 있을 때만 저장합니다.
            this.setData(Attachments.VOID_ANCHOR_POSITION, value)
        }
    }
