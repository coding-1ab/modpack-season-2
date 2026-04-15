package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.block_entity_config;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes the distance check in Create block entity configuration to take sublevels into account
 */
@Mixin(BlockEntityConfigurationPacket.class)
public class BlockEntityConfigurationPacketMixin {

    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"))
    private boolean sable$closerThan(final BlockPos instance, final Vec3i pVector, final double distance, @Local(argsOnly = true) final ServerPlayer player) {
        if (instance.closerThan(pVector, distance)) return true;

        return Sable.HELPER.distanceSquaredWithSubLevels(
                player.level(),
                instance.getX(),
                instance.getY(),
                instance.getZ(),
                pVector.getX(),
                pVector.getY(),
                pVector.getZ()) < distance * distance;
    }

}
