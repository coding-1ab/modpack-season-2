package org.antarcticgardens.cna;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

import java.util.function.Consumer;

public abstract class Platform {
    public abstract PlatformRegistrar getRegistrar();
    public abstract void commonSetup(Runnable commonSetup);
    public abstract Object getEnergisingRecipeSubCategory();
    
    public abstract void subscribeClientTickEnd(Runnable runnable);
    public abstract void subscribeRenderLayerEvent(Consumer<RenderLayerEvent> consumer);
    public abstract void subscribeBeginFrameEvent(Consumer<BeginFrameEvent> consumer);
    public abstract void subscribeReloadRenderersEvent(Consumer<ReloadRenderersEvent> consumer);
}
