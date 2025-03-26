package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.texture.VeilPreloadedTexture;
import foundry.veil.ext.TextureManagerExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureManager.class)
public abstract class PipelineTextureManagerMixin implements TextureManagerExtension {

    @Shadow
    @Final
    private Map<ResourceLocation, AbstractTexture> byPath;

    @Shadow
    @Final
    private ResourceManager resourceManager;

    @Shadow
    public abstract void register(ResourceLocation path, AbstractTexture texture);

    @Override
    public <T extends AbstractTexture & VeilPreloadedTexture> CompletableFuture<?> veil$registerPreloadedTexture(ResourceLocation path, T texture, Executor executor) {
        if (!this.byPath.containsKey(path)) {
            this.byPath.put(path, texture);
            return texture.preload(this.resourceManager, executor).thenRunAsync(() -> this.register(path, texture), command -> Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(command::run)));
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
}
