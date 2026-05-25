package dev.codinglabs.modpack.rapier_entity.network

import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.rapier_entity.network.packets.AttachBoxes
import dev.codinglabs.modpack.rapier_entity.network.packets.BoxData
import dev.codinglabs.modpack.rapier_entity.network.packets.PacketTypes
import dev.codinglabs.modpack.rapier_entity.network.packets.UdpEncodable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelOutboundHandler
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.EncoderException
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.flow.FlowControlHandler
import net.minecraft.network.*
import net.minecraft.util.debugchart.LocalSampleLogger
import java.io.IOException
import java.net.InetSocketAddress

val monitor = BandwidthDebugMonitor(LocalSampleLogger(512))

data class UdpPacket(
    val data: UdpEncodable,
) {
    companion object {
        fun decode(buffer: FriendlyByteBuf): UdpPacket {
            val packetId = buffer.readByte().toInt()
            if (packetId >= PacketTypes.entries.size) {
                throw IOException("Received an invalid packet ID: $packetId")
            }

            val type = PacketTypes.entries[packetId]
            val packet = when(type) {
                PacketTypes.ATTACH -> {
                    val headId = buffer.readInt()
                    val boxes = buffer.readArray(::arrayOfNulls, BoxData.STREAM_CODEC).toList()
                    UdpPacket(AttachBoxes(headId, boxes))
                }
            }

            if (buffer.readableBytes() > 0) {
                ModPackTweaks.LOGGER.error("{} received {} more bytes than than expected",
                    type,
                    buffer.readableBytes()
                )
            }

            return packet
        }
    }

    fun encode(buffer: FriendlyByteBuf) {
        buffer.writeByte(data.type.ordinal)
        when(data.type) {
            PacketTypes.ATTACH -> {
                val attach = data as AttachBoxes
                buffer.writeInt(attach.headId)
                val asArray = attach.boxes.toTypedArray()
                buffer.writeArray(asArray, BoxData.STREAM_CODEC)
            }
        }
    }
}

data class AddressedUdpPacket(val packet: UdpPacket, val address: InetSocketAddress)

fun setupSerialization(pipeline: ChannelPipeline, isLocal: Boolean) {
    pipeline.addLast("splitter", createFrameDecoder(isLocal))
        .addLast(FlowControlHandler())
        .addLast("decoder", Decoder)
        .addLast("prepender", createFrameEncoder(isLocal))
        .addLast("encoder", Encoder)
}

private object Decoder : MessageToMessageDecoder<DatagramPacket>(DatagramPacket::class.java), ProtocolSwapHandler {
    override fun decode(
        ctx: ChannelHandlerContext,
        msg: DatagramPacket,
        out: MutableList<Any?>
    ) {
        val bytes = msg.content()
        if (bytes.readableBytes() == 0) {
            return
        }

        val buffer = FriendlyByteBuf(bytes)
        val packet = UdpPacket.decode(buffer)

        out.add(AddressedUdpPacket(packet, msg.sender()))
    }

}

private object Encoder: MessageToMessageEncoder<AddressedUdpPacket>() {
    override fun encode(
        ctx: ChannelHandlerContext,
        msg: AddressedUdpPacket,
        out: MutableList<Any?>
    ) {
        try {
            val ioBuffer = ctx.alloc().ioBuffer()
            val asFriendly = FriendlyByteBuf(ioBuffer)
            msg.packet.encode(asFriendly)
            out.add(DatagramPacket(asFriendly, msg.address))
        } catch (e: Exception) {
            throw EncoderException("Failed to encode ${msg::class.simpleName} packet of type ${msg.packet.data.type}", e)
        }
    }

}

fun createFrameEncoder(isLocal: Boolean): ChannelOutboundHandler {
    return if (isLocal) {
        NoOpFrameEncoder()
    } else {
        Varint21LengthFieldPrepender()
    }
}

fun createFrameDecoder(isLocal: Boolean): ChannelInboundHandler {
    return if (!isLocal) {
        Varint21FrameDecoder(null)
    } else {
        MonitorFrameDecoder(monitor)
    }
}
