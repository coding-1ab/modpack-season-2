package dev.codinglabs.modpack.rapier_entity.network

import dev.codinglabs.modpack.rapier_entity.network.packets.BoxCreation
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

fun registerTcpPackets(event: RegisterPayloadHandlersEvent) {
    val registrar = event.registrar("1")
    registrar.playToClient(
        BoxCreation.PAYLOAD_TYPE,
        BoxCreation.STREAM_CODEC,
        BoxCreation::onReceive
    )
}
