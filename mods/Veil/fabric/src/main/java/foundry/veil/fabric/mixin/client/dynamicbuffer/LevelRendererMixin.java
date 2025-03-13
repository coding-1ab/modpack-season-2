package foundry.veil.fabric.mixin.client.dynamicbuffer;

import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 200) // Apply these changes last
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=destroyProgress"))
    public void beginTranslucent(CallbackInfo ci) {
        VeilRenderSystem.renderer().getDynamicBufferManger().setEnabled(true);
    }
}
