package foundry.veil.forge.mixin.client;

import com.google.common.collect.ImmutableList;
import foundry.veil.Veil;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.forge.event.ForgeFreeNativeResourcesEvent;
import foundry.veil.forge.event.ForgeVeilRegisterBlockLayersEvent;
import foundry.veil.forge.impl.ForgeRenderTypeStageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    public void registerBlockLayers(CallbackInfo ci) {

        Set<RenderType> blockLayers = new HashSet<>();
        ModLoader.postEvent(new ForgeVeilRegisterBlockLayersEvent(renderType -> {
            if (Veil.platform().isDevelopmentEnvironment() && renderType.bufferSize() > RenderType.SMALL_BUFFER_SIZE) {
                Veil.LOGGER.warn("Block render layer '{}' uses a large buffer size: {}. If this is intended you can ignore this message", VeilRenderType.getName(renderType), renderType.bufferSize());
            }
            blockLayers.add(renderType);
        }));
        ForgeRenderTypeStageHandler.setBlockLayers(blockLayers);
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;shutdownExecutors()V", shift = At.Shift.BEFORE))
    public void close(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ForgeFreeNativeResourcesEvent());
    }
}
