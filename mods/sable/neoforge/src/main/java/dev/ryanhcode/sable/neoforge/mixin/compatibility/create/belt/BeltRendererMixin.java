package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Make upright items face the the camera properly on belts
 */
@Mixin(BeltRenderer.class)
public class BeltRendererMixin {

    @WrapOperation(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 sable$renderViewEntityPosition(
            final Camera instance,
            final Operation<Vec3> original,
            @Local(argsOnly = true) final BeltBlockEntity be
    ) {
        final Vec3 pos = original.call(instance);

        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(be);
        if (subLevel != null) {
            return subLevel.renderPose().transformPositionInverse(pos);
        }

        return pos;
    }
}
