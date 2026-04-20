package dev.ryanhcode.sable.mixin.compatibility.computercraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dan200.computercraft.api.network.PacketReceiver;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessNetwork;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WirelessNetwork.class)
public class WirelessNetworkMixin {

    @WrapOperation(
            remap = false,
            method = "tryTransmit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"
            )
    )
    private static double getPosition(
            final Vec3 a,
            final Vec3 b,
            Operation<Double> original,
            @Local(ordinal = 0, argsOnly = true) final PacketReceiver packetReceiver
    ) {
        Level level = packetReceiver.getLevel();
        final Vec3 globalA = Sable.HELPER.projectOutOfSubLevel(level, a);
        final Vec3 globalB = Sable.HELPER.projectOutOfSubLevel(level, b);

        return original.call(globalA, globalB);
    }

}
