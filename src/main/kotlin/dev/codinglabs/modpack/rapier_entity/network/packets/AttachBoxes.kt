package dev.codinglabs.modpack.rapier_entity.network.packets

import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.rapier_entity.BoxedEntity
import dev.codinglabs.modpack.rapier_entity.network.UdpHandlerServer
import dev.codinglabs.modpack.toResource
import dev.ryanhcode.sable.api.physics.`object`.box.BoxPhysicsObject
import dev.ryanhcode.sable.companion.math.Pose3d
import io.netty.buffer.ByteBuf
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.joml.Vector3d

data class AttachBoxes(
    val headId: Int,
    val boxes: List<BoxData>,

): CustomPacketPayload, UdpEncodable {
    companion object {
        val ID = "box_creation".toResource()
        val PAYLOAD_TYPE = CustomPacketPayload.Type<AttachBoxes>(ID)

        val STREAM_CODEC: StreamCodec<ByteBuf, AttachBoxes> = StreamCodec.composite(
            ByteBufCodecs.INT,
            AttachBoxes::headId,
            BoxData.STREAM_CODEC.apply(ByteBufCodecs.list(64)),
            AttachBoxes::boxes,
            ::AttachBoxes
        )
    }

    init {
        if (boxes.isEmpty()) {
            ModPackTweaks.LOGGER.warn("Generated empty AttachBoxes packet!")
        }
    }

    override fun type() = PAYLOAD_TYPE

    fun onTcpReceive(context: IPayloadContext) {
        onReceive(UdpHandlerServer.UdpPacketContext(context.player()))
    }

    override val type: PacketTypes
        get() = PacketTypes.ATTACH

    override fun onReceive(context: UdpHandlerServer.UdpPacketContext) {
        val mainPlayer = context.player as LocalPlayer
        val entity = mainPlayer.clientLevel.getEntity(headId)
        if (entity == null) {
            ModPackTweaks.LOGGER.error("Received  box attachment packet for non synched entity with id $headId")
            return
        }

        val boxEntity = entity as? BoxedEntity
        if (boxEntity == null) {
            ModPackTweaks.LOGGER.error("Entity with id $headId is not BoxedEntity! Instead it's: $entity")
            return
        }

        boxEntity.boxes.addAll(boxes.map { box ->
            BoxPhysicsObject(box.pose, box.halfExtents, box.mass)
        })
    }
}

data class BoxData(
    val pose: Pose3d = Pose3d(),
    val halfExtents: Vector3d = Vector3d(),
    val mass: Double = 0.0,
) {
    companion object {
        val STREAM_CODEC: StreamCodec<ByteBuf, BoxData> = StreamCodec.composite(
            StreamCodecs.POSE3D,
            BoxData::pose,
            StreamCodecs.VECTOR3D,
            BoxData::halfExtents,
            ByteBufCodecs.DOUBLE,
            BoxData::mass,
            ::BoxData
        )
    }
}
