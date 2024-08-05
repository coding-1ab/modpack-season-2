package org.antarcticgardens.cna.mixin;

import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.cna.rendering.fallbackInstance.BlockEntityExtension;
import org.antarcticgardens.cna.rendering.fallbackInstance.FallbackInstanceRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements BlockEntityExtension {
    @Unique private FallbackInstanceRenderer<?> create_new_age$fallbackRenderer;
    @Unique private BlockEntityInstance<?> create_new_age$fallbackInstance;

    @Override
    public BlockEntityInstance<?> create_new_age$getFallbackInstance() {
        return create_new_age$fallbackInstance;
    }

    @Override
    public void create_new_age$setFallbackInstance(FallbackInstanceRenderer<?> renderer, BlockEntityInstance<?> instance) {
        create_new_age$fallbackRenderer = renderer;
        create_new_age$fallbackInstance = instance;
    }

    @Inject(method = "setRemoved", at = @At("RETURN"))
    private void setRemoved(CallbackInfo ci) {
        if (create_new_age$fallbackInstance != null) 
            create_new_age$fallbackRenderer.removeInstance((BlockEntity) (Object) this);
    }
}
