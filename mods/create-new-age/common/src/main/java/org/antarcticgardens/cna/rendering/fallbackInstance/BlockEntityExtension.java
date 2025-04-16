package org.antarcticgardens.cna.rendering.fallbackInstance;


import dev.engine_room.flywheel.api.visual.BlockEntityVisual;

public interface BlockEntityExtension {
    BlockEntityVisual<?> create_new_age$getFallbackInstance();
    void create_new_age$setFallbackInstance(FallbackInstanceRenderer<?> renderer, BlockEntityVisual<?> instance);
}
