package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.ext.RenderTargetExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTarget.class)
public abstract class PipelineRenderTargetMixin implements RenderTargetExtension {

    @Shadow
    public int frameBufferId;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public int viewWidth;
    @Shadow
    public int viewHeight;

    @Unique
    private AdvancedFbo veil$wrapper;
    @Unique
    private int veil$vanillaFramebufferId;
    @Unique
    private int veil$vanillaWidth;
    @Unique
    private int veil$vanillaHeight;
    @Unique
    private int veil$vanillaViewWidth;
    @Unique
    private int veil$vanillaViewHeight;

    @Override
    public void veil$setWrapper(@Nullable AdvancedFbo fbo) {
        // If the state has changed
        if (this.veil$wrapper == null ^ fbo != null) {
            if (fbo != null) {
                // Save state
                this.veil$vanillaFramebufferId = this.frameBufferId;
                this.veil$vanillaWidth = this.width;
                this.veil$vanillaHeight = this.height;
                this.veil$vanillaViewWidth = this.viewWidth;
                this.veil$vanillaViewHeight = this.viewHeight;

                this.frameBufferId = fbo.getId();
                this.width = fbo.getWidth();
                this.height = fbo.getHeight();
                this.viewWidth = this.width;
                this.viewHeight = this.height;
            } else {
                // Load state
                this.frameBufferId = this.veil$vanillaFramebufferId;
                this.width = this.veil$vanillaWidth;
                this.height = this.veil$vanillaHeight;
                this.viewWidth = this.veil$vanillaViewWidth;
                this.viewHeight = this.veil$vanillaViewHeight;
            }
        }
        this.veil$wrapper = fbo;
    }

    @Override
    public int veil$getTexture(int buffer) {
        if (this.veil$wrapper != null && this.veil$wrapper.isColorTextureAttachment(buffer)) {
            return this.veil$wrapper.getColorTextureAttachment(buffer).getId();
        }
        return -1;
    }

    @Inject(method = "bindRead", at = @At("HEAD"), cancellable = true)
    public void bindRead(CallbackInfo ci) {
        if (this.veil$wrapper != null) {
            if (this.veil$wrapper.isColorTextureAttachment(0)) {
                this.veil$wrapper.getColorTextureAttachment(0).bind();
            }
            ci.cancel();
        }
    }

    @Inject(method = "bindWrite", at = @At("HEAD"), cancellable = true)
    public void bindWrite(boolean setViewport, CallbackInfo ci) {
        if (this.veil$wrapper != null) {
            this.veil$wrapper.bind(setViewport);
            ci.cancel();
        }
    }

    @Inject(method = "getColorTextureId", at = @At("HEAD"), cancellable = true)
    public void getColorTextureId(CallbackInfoReturnable<Integer> cir) {
        if (this.veil$wrapper != null) {
            cir.setReturnValue(this.veil$wrapper.getColorTextureAttachment(0).getId());
        }
    }

    @Inject(method = "getDepthTextureId", at = @At("HEAD"), cancellable = true)
    public void getDepthTextureId(CallbackInfoReturnable<Integer> cir) {
        if (this.veil$wrapper != null) {
            cir.setReturnValue(this.veil$wrapper.getDepthTextureAttachment().getId());
        }
    }
}
