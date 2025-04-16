package org.antarcticgardens.cna;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.simibubi.create.compat.recipeViewerCommon.SequencedAssemblySubCategoryType;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.antarcticgardens.cna.fabric.FabricRegistrar;
import org.antarcticgardens.cna.fabric.compat.emi.EmiEnergisingSubcategory;
import org.antarcticgardens.cna.fabric.compat.jei.FabricJeiEnergisingSubcategory;
import org.antarcticgardens.cna.fabric.compat.rei.ReiEnergiserSubcategory;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

import java.util.function.Consumer;

public class FabricPlatform extends Platform {
    private final FabricRegistrar registrar = new FabricRegistrar();

    @Override
    public PlatformRegistrar getRegistrar() {
        return registrar;
    }

    @Override
    public void commonSetup(Runnable commonSetup) {
        commonSetup.run();
    }

    @Override
    public Object getEnergisingRecipeSubCategory() {
        return new SequencedAssemblySubCategoryType(
                () -> FabricJeiEnergisingSubcategory::new,
                () -> ReiEnergiserSubcategory::new,
                () -> EmiEnergisingSubcategory::new
        );
    }

    
    @Override
    public void subscribeClientTickEnd(Runnable runnable) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> runnable.run());
    }
    
    @Override
    public void subscribeRenderLayerEvent(Consumer<RenderLevelStageEvent> consumer) {
        FlywheelEvents.RENDER_LAYER.register(consumer::accept);
    }

    @Override
    public void subscribeBeginFrameEvent(Consumer<BeginFrameEvent> consumer) {
        FlywheelEvents.BEGIN_FRAME.register(consumer::accept);
    }

    @Override
    public void subscribeReloadRenderersEvent(Consumer<ReloadLevelRendererEvent> consumer) {
        FlywheelEvents.RELOAD_RENDERERS.register(consumer::accept);
    }
}
