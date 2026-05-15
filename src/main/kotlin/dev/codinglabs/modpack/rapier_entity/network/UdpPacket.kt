package dev.codinglabs.modpack.rapier_entity.network

import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.rapier_entity.network.packets.PacketTypes
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
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.util.debugchart.LocalSampleLogger
import java.io.IOException
import java.net.InetSocketAddress

val monitor = BandwidthDebugMonitor(LocalSampleLogger(512))

data class UdpPacket(
    val type: PacketTypes
) {
    fun encode(buffer: FriendlyByteBuf) {

    }
}

data class AddressedUdpPacket(val packet: UdpPacket, val address: InetSocketAddress)

// Server-bound: destination is server
// Client-bound: destination is client
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

        val packetId = bytes.readUnsignedByte()
        if (packetId >= PacketTypes.entries.size) {
            throw IOException("Received an invalid packet ID: $packetId")
        }

        val packetType = PacketTypes.entries[packetId.toInt()]
        val packet = try {
            packetType.decode(FriendlyByteBuf(bytes))
        } catch (e: Exception) {
            ModPackTweaks.LOGGER.error("Error while decoding packet: $packetType", e)
            return
        }

        if (bytes.readableBytes() > 0) {
            ModPackTweaks.LOGGER.error("{} received {} more bytes than than expected", packetType,bytes.readableBytes())
            return
        }

        out.add(AddressedUdpPacket(packet, msg.sender()))
    }

}

private object Encoder: MessageToMessageEncoder<AddressedUdpPacket>() {
    override fun encode(
        ctx: ChannelHandlerContext,
        msg: AddressedUdpPacket,
        out: MutableList<Any?>
    ) {
        val packet = msg.packet
        val type = packet.type

        try {
            val buffer = ctx.alloc().ioBuffer()
            buffer.writeByte(type.ordinal)
            packet.encode(FriendlyByteBuf(buffer))
            out.add(DatagramPacket(buffer, msg.address))
        } catch (e: Exception) {
            throw EncoderException("Failed to encode ${msg::class.simpleName} packet of type $type", e)
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
        Varint21FrameDecoder(null);
    } else {
        MonitorFrameDecoder(monitor)
    }
}
