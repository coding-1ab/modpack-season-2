package foundry.veil.fabric.mixin.client;

import com.google.common.collect.ImmutableList;
import foundry.veil.fabric.FabricRenderTypeStageHandler;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Shadow
    @Final
    private static ImmutableList<RenderType> CHUNK_BUFFER_LAYERS;

    @Inject(method = "chunkBufferLayers", at = @At("HEAD"), cancellable = true)
    private static void injectChunkBufferLayers(CallbackInfoReturnable<List<RenderType>> cir) {
        List<RenderType> renderTypes = FabricRenderTypeStageHandler.getBlockLayers(CHUNK_BUFFER_LAYERS);
        if (renderTypes != null) {
            cir.setReturnValue(renderTypes);
        }
    }
}
