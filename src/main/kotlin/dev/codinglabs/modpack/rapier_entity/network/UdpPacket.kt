package dev.codinglabs.modpack.rapier_entity

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelOutboundHandler
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.flow.FlowControlHandler
import net.minecraft.network.BandwidthDebugMonitor
import net.minecraft.network.MonitorFrameDecoder
import net.minecraft.network.NoOpFrameEncoder
import net.minecraft.network.ProtocolSwapHandler
import net.minecraft.network.Varint21FrameDecoder
import net.minecraft.network.Varint21LengthFieldPrepender
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.util.debugchart.LocalSampleLogger

val monitor = BandwidthDebugMonitor(LocalSampleLogger(512))

data class UdpPacket()

// Server-bound: destination is server
// Client-bound: destination is client
fun setupSerialization(pipeline: ChannelPipeline, flow: PacketFlow, isLocal: Boolean) {
    pipeline.addLast("splitter", createFrameDecoder(isLocal))
        .addLast(FlowControlHandler())
        .addLast("decoder", SableUDPPacketDecoder())
        .addLast("prepender", createFrameEncoder(isLocal))
        .addLast("encoder", SableUDPPacketEncoder())
}

object Decoder : MessageToMessageDecoder<DatagramPacket>(DatagramPacket::class.java), ProtocolSwapHandler {
    override fun decode(
        ctx: ChannelHandlerContext,
        msg: DatagramPacket,
        out: List<Any>
    ) {
        val bytes = msg.content()
        if (bytes.readableBytes() == 0) {
            return
        }

        val packetId = 
    }

}

object Encoder {

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
