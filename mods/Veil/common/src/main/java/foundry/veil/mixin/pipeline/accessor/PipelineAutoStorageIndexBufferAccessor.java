package foundry.veil.mixin.pipeline.accessor;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.AutoStorageIndexBuffer.class)
public interface PipelineAutoStorageIndexBufferAccessor {

    @Accessor
    int getName();
}
