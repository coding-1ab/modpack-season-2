package dev.codinglabs.modpack.rapier_entity.network

import dev.codinglabs.modpack.rapier_entity.network.packets.AttachBoxes
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

fun registerTcpPackets(event: RegisterPayloadHandlersEvent) {
    val registrar = event.registrar("1")
    registrar.playToClient(
        AttachBoxes.PAYLOAD_TYPE,
        AttachBoxes.STREAM_CODEC,
        AttachBoxes::onTcpReceive
    )
}
