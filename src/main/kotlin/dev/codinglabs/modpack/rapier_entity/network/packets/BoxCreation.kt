package dev.codinglabs.modpack.rapier_entity.network.packets

import dev.codinglabs.modpack.toResource
import dev.ryanhcode.sable.companion.math.Pose3d
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.joml.Vector3d
import kotlin.apply

data class BoxCreation(
    val headId: Int,
    val boxes: List<BoxData>,

): CustomPacketPayload {
    companion object {
        val ID = "box_creation".toResource()
        val PAYLOAD_TYPE = CustomPacketPayload.Type<BoxCreation>(ID)

        val STREAM_CODEC: StreamCodec<ByteBuf, BoxCreation> = StreamCodec.composite(
            ByteBufCodecs.INT,
            BoxCreation::headId,
            BoxData.STREAM_CODEC.apply(ByteBufCodecs.list(64)),
            BoxCreation::boxes,
            ::BoxCreation
        )
    }

    override fun type() = PAYLOAD_TYPE

    fun onReceive(context: IPayloadContext) {

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
