package dev.codinglabs.modpack.rapier_entity.network.packets

import dev.codinglabs.modpack.rapier_entity.network.UdpPacket
import net.minecraft.network.FriendlyByteBuf

enum class PacketTypes {
    CREATE;

    fun encode(buffer: FriendlyByteBuf) {

    }

    fun decode(buffer: FriendlyByteBuf): UdpPacket {
        throw AssertionError("unimplemented")
    }
}
