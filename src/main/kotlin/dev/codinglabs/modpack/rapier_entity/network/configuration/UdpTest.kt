package dev.codinglabs.modpack.rapier_entity.network.configuration

import dev.codinglabs.modpack.config.Config
import dev.codinglabs.modpack.toResource
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext

// 클라이언트가 UDP 연결을 승인하는지 물어보는 패킷
data object RequestUdpChannel: CustomPacketPayload {
    val ID = "request_udp".toResource()
    val PAYLOAD_TYPE = CustomPacketPayload.Type<RequestUdpChannel>(ID)

    val STREAM_CODEC: StreamCodec<ByteBuf, RequestUdpChannel> = StreamCodec.unit(this)

    fun onReceive(context: IPayloadContext) {
        PacketDistributor.sendToServer(UdpChannelResponse(Config.ENABLE_UDP.get()))
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = PAYLOAD_TYPE
}

data class UdpChannelResponse(val useUdp: Boolean) : CustomPacketPayload {
    companion object {
        val ID = "response_udp".toResource()
        val PAYLOAD_TYPE = CustomPacketPayload.Type<UdpChannelResponse>(ID)

        val STREAM_CODEC: StreamCodec<ByteBuf, UdpChannelResponse> = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            UdpChannelResponse::useUdp,
            ::UdpChannelResponse
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = PAYLOAD_TYPE

    fun onReceive(context: IPayloadContext) {

    }
}
