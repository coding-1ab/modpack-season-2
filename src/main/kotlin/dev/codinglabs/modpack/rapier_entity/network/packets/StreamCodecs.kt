package dev.codinglabs.modpack.rapier_entity.network.packets

import dev.ryanhcode.sable.companion.math.Pose3d
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc

object StreamCodecs {
    val VECTOR3D: StreamCodec<ByteBuf, Vector3d> = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        Vector3dc::x,
        ByteBufCodecs.DOUBLE,
        Vector3dc::y,
        ByteBufCodecs.DOUBLE,
        Vector3dc::z,
        ::Vector3d
    )

    val QUATERNIOND: StreamCodec<ByteBuf, Quaterniond> = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        Quaterniondc::x,
        ByteBufCodecs.DOUBLE,
        Quaterniondc::y,
        ByteBufCodecs.DOUBLE,
        Quaterniondc::z,
        ByteBufCodecs.DOUBLE,
        Quaterniondc::w,
        ::Quaterniond,
    )

    val POSE3D: StreamCodec<ByteBuf, Pose3d> = StreamCodec.composite(
        VECTOR3D,
        Pose3d::position,
        QUATERNIOND,
        Pose3d::orientation,
        VECTOR3D,
        Pose3d::rotationPoint,
        VECTOR3D,
        Pose3d::scale,
        ::Pose3d
    )
}
