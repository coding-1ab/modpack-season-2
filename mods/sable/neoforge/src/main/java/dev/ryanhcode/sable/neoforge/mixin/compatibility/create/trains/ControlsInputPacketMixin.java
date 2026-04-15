package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.trains;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsInputPacket;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes the range check in Create's {@link ControlsInputPacket} for trains to account for sub-levels.
 */
@Mixin(ControlsInputPacket.class)
public class ControlsInputPacketMixin {

    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z"))
    private boolean sable$projectComparison(final Vec3 controllerPos, final Position playerPos, final double pDistance, @Local(argsOnly = true) final ServerPlayer player) {
        return Sable.HELPER.distanceSquaredWithSubLevels(player.level(), controllerPos, playerPos.x(), playerPos.y(), playerPos.z()) < pDistance * pDistance;
    }

}
