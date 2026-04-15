package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.logistics;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedClientHandler;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LogisticallyLinkedClientHandler.class, remap = false)
public class LogisticallyLinkedClientHandlerMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"))
    private static boolean sable$projectComparison(final BlockPos instance, final Vec3i vec3i, final double v, @Local final LocalPlayer player) {
        return Sable.HELPER.distanceSquaredWithSubLevels(player.level(), instance.getX(), instance.getY(), instance.getZ(), vec3i.getX(), vec3i.getY(), vec3i.getZ()) < v * v;
    }

}
