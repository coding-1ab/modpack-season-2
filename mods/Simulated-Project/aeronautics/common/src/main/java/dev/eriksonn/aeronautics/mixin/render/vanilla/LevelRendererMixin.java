package dev.eriksonn.aeronautics.mixin.render.vanilla;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.eriksonn.aeronautics.content.blocks.levitite.LevititeShaderManager;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 990)
public class LevelRendererMixin {

    @Inject(method = "renderSectionLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;apply()V", shift = At.Shift.AFTER))
    public void aeronautics$setupLevititeShaders(RenderType renderType, double x, double y, double z, Matrix4f frustrumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local ShaderInstance shaderinstance, @Local LocalBooleanRef flag1) {
        if (renderType == AeroRenderTypes.levititeGhosts()) {
            flag1.set(false); // skip rendering
        } else if (renderType == AeroRenderTypes.levitite()) {
            long ticks = Minecraft.getInstance().level.getGameTime();
            final float pt = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            ticks = ticks % 100000;

            shaderinstance.safeGetUniform("time").set(ticks + pt);
            LevititeShaderManager.prepareShaderForWorld(shaderinstance, x, y, z);
        }
    }

    @Inject(method = "renderSectionLayer", at = @At(value = "TAIL"))
    public void aeronautics$cleanupLevititeShaders(RenderType renderType, double x, double y, double z, Matrix4f frustrumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local ShaderInstance shaderinstance) {
        if (renderType == AeroRenderTypes.levitite()) {
            // reset back to world once rendering is done, for safety
            LevititeShaderManager.prepareShaderForWorld(shaderinstance, x, y, z);
        }
    }

}
