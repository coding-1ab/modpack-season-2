package dev.codinglabs.modpack.mixin_interfaces

import dev.codinglabs.modpack.rapier_entity.network.UdpServer
import io.netty.channel.Channel

interface ServerConnectionListenerExtension {
    fun `codinglab$setupUdpServer`(channel: Channel)

    val `codinglab$server`: UdpServer
}
