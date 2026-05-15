package dev.codinglabs.modpack.rapier_entity.network.packets

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

object Codecs {
    val VECTOR3D = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        
    )
}
