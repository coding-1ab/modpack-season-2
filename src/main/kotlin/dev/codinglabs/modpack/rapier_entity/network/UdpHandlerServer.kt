package dev.codinglabs.modpack.rapier_entity.network

import dev.codinglabs.modpack.mixin_interfaces.ServerConnectionListenerExtension
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerConnectionListener
import net.minecraft.world.entity.player.Player

class UdpHandlerServer(
    val server: MinecraftServer,
    val listener: ServerConnectionListener
): SimpleChannelInboundHandler<AddressedUdpPacket>(AddressedUdpPacket::class.java) {
    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        listener as ServerConnectionListenerExtension

        listener.`codinglab$setupUdpServer`(ctx.channel())
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: AddressedUdpPacket
    ) {
        val player = server.playerList.players.find { player -> player.connection.remoteAddress == msg.address }
        // msg.packet.data.onReceive()
    }

    data class UdpPacketContext(
        val player: Player
    )
}
