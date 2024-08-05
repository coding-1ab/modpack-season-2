package org.antarcticgardens.cna.rendering.fallbackInstance;

import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;

public interface BlockEntityExtension {
    BlockEntityInstance<?> create_new_age$getFallbackInstance();
    void create_new_age$setFallbackInstance(FallbackInstanceRenderer<?> renderer, BlockEntityInstance<?> instance);
}
