package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.belt;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Make upright items face the the camera properly on belts
 */
@Mixin(BeltRenderer.class)
public class BeltRendererMixin {

    @Redirect(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;position()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 sable$renderViewEntityPosition(final Entity instance, @Local(argsOnly = true) final float partialTicks, @Local(argsOnly = true) final BeltBlockEntity be) {
        final Vec3 pos = instance.getPosition(partialTicks);

        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(be);
        if (subLevel != null) {
            return subLevel.renderPose().transformPositionInverse(pos);
        }

        return pos;
    }
}
