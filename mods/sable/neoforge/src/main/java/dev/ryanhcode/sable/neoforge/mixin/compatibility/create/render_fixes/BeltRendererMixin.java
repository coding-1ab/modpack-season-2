package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.render_fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeltRenderer.class)
public class BeltRendererMixin {

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"))
    public double sable$projectDistanceTo(final Vec3 eyePos, final Vec3 itemPos, Operation<Double> original) {
        Level level = Minecraft.getInstance().level;
        final Vec3 projectedEyePos = Sable.HELPER.projectOutOfSubLevel(level, eyePos);
        final Vec3 projectedItemPos = Sable.HELPER.projectOutOfSubLevel(level, itemPos);

        return original.call(projectedEyePos, projectedItemPos);
    }
}
