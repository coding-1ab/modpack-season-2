package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.depot;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DepotRenderer.class)
public class DepotRendererMixin {

    @Redirect(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;position()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 sable$renderViewEntityPosition(final Entity instance, @Local(argsOnly = true) final Vec3 position) {
        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        final SubLevel subLevel = Sable.HELPER.getContaining(instance.level(), position);

        if (subLevel instanceof final ClientSubLevel clientSubLevel) {
            pos = clientSubLevel.renderPose().transformPositionInverse(pos);
        }

        return pos;
    }

}
