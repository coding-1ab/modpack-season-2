package dev.codinglabs.modpack.rapier_entity.network.packets

import dev.codinglabs.modpack.rapier_entity.network.UdpHandlerServer
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

enum class PacketTypes {
    ATTACH,;
}

sealed interface UdpEncodable: CustomPacketPayload {
    val type: PacketTypes

    fun onReceive(context: UdpHandlerServer.UdpPacketContext)
}
