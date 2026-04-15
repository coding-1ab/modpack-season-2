package dev.ryanhcode.sable.mixin.compatibility.vista;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LOD.class)
public class LODMixin {

    @Unique
    private static final Vector3d sable$direction = new Vector3d();

    @Shadow
    @Final
    @Mutable
    private Vec3 objCenter;

    @Shadow
    @Final
    @Mutable
    private double distSq;

    @Shadow
    @Final
    private Vec3 cameraPosition;

    @WrapOperation(method = "isPlaneCulled(Lnet/minecraft/core/Direction;FFF)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;step()Lorg/joml/Vector3f;"))
    private Vector3f sable$step(final Direction instance, final Operation<Vector3f> original) {
        final Vector3f dir = original.call(instance);
        final ClientSubLevel clientSubLevel = Sable.HELPER.getContainingClient(this.objCenter);
        if(clientSubLevel != null) {
            sable$direction.set(dir);
            clientSubLevel.renderPose().transformNormal(sable$direction);
            dir.set(sable$direction);
        }
        return dir;
    }

    @Inject(method = "<init>(Lnet/minecraft/client/Camera;Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void sable$init(Camera camera, Vec3 objCenter, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        this.objCenter = Sable.HELPER.projectOutOfSubLevel(level, objCenter);
        this.distSq = LOD.isScoping() ? (double) 1.0F : Sable.HELPER.distanceSquaredWithSubLevels(level, this.cameraPosition, objCenter);
    }
}
