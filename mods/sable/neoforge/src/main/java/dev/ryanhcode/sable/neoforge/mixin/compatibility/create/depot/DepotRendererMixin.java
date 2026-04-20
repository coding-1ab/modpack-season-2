package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.depot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DepotRenderer.class)
public class DepotRendererMixin {

    @WrapOperation(
            method = "renderItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/item/ItemStack;ILjava/util/Random;Lnet/minecraft/world/phys/Vec3;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private static Vec3 sable$renderViewEntityPosition(final Camera instance, final Operation<Vec3> original, @Local(argsOnly = true) final Vec3 position) {
        Vec3 pos = original.call(instance);
        Level level = Minecraft.getInstance().level;

        final SubLevel subLevel = Sable.HELPER.getContaining(level, position);

        if (subLevel instanceof final ClientSubLevel clientSubLevel) {
            pos = clientSubLevel.renderPose().transformPositionInverse(pos);
        }

        return pos;
    }

}
