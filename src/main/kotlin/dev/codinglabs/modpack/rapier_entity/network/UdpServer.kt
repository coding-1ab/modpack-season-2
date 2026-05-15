package dev.codinglabs.modpack.rapier_entity.network

import dev.ryanhcode.sable.network.udp.SableUDPServer
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraft.server.MinecraftServer

/**
 * 엔티티 모델을 동기화 하기 위한 UDP 채널
 * [SableUDPServer]를 따라 만든 것임.
 * @author RyanH, Ocelot, tmvkrpxl0
 */
class UdpServer(
    private val server: MinecraftServer,
    private val channel: Channel
) {
    companion object {
        const val PING_INTERVAL = 2500
        const val MISSED_PINGS_ALLOWED = 10
    }
}

class UdpChannelFilter: SimpleChannelInboundHandler<AddressedUdpPacket>(AddressedUdpPacket::class.java) {
    override fun channelRead0(
        ctx: ChannelHandlerContext?,
        msg: AddressedUdpPacket?
    ) {
        TODO("Not yet implemented")
    }

}
