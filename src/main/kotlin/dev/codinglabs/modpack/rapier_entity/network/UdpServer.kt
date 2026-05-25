package dev.codinglabs.modpack.rapier_entity.network

import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.client.getLocalPlayer
import dev.codinglabs.modpack.config.Config
import dev.codinglabs.modpack.mixin_interfaces.ServerConnectionListenerExtension
import dev.ryanhcode.sable.network.udp.SableUDPServer
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.local.LocalAddress
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus
import java.net.InetSocketAddress

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

        @ApiStatus.Internal
        fun getServer(server: MinecraftServer): UdpServer {
            val connection = server.connection as ServerConnectionListenerExtension

            return connection.`codinglab$server`
        }
    }

    private val udpAddresses = HashMap<Connection, InetSocketAddress>()

    val ServerPlayer.udpAddress: InetSocketAddress?
        get() {
            if (!Config.ENABLE_UDP.get()) {
                return null
            }

            if (connection.remoteAddress is LocalAddress) {
                if (server.isSingleplayer && server.isSingleplayerOwner(gameProfile))
                    return null
            }

            return this@UdpServer.udpAddresses[connection.connection]
        }

    fun send(player: ServerPlayer, packet: UdpPacket, flush: Boolean) {
        if (channel.eventLoop().inEventLoop()) {
            throw IllegalStateException("Cannot send packet from event loop")
        }

        val connection = player.connection.connection

        if (connection.remoteAddress is LocalAddress) {
            sendLocal(packet)
            return
        }

        val udpAddress = player.udpAddress
        if (udpAddress == null) {
            ModPackTweaks.LOGGER.error("Player does not accepts udp channel")
            return
        }

        channel.eventLoop().execute {
            val envelope = AddressedUdpPacket(packet, udpAddress)
            val writeFuture = if (flush) {
                this.channel.writeAndFlush(envelope)
            } else {
                this.channel.write(envelope)
            }
            writeFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        }
    }

    fun sendLocal(packet: UdpPacket) {
        val buffer = FriendlyByteBuf(Unpooled.buffer())
        packet.encode(buffer)
        val packetCopy = UdpPacket.decode(buffer)
        val context = UdpHandlerServer.UdpPacketContext(getLocalPlayer()!!)
        Connection.LOCAL_WORKER_GROUP.get().execute {
            packetCopy.data.onReceive(context)
        }
    }
}
