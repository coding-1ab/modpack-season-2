package org.antarcticgardens.cna;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.antarcticgardens.cna.forge.ForgeRegistrar;
import org.antarcticgardens.cna.forge.compat.jei.ForgeJeiEnergisingSubcategory;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgePlatform extends Platform {
    private final ForgeRegistrar registrationHelper = new ForgeRegistrar();
    
    @Override
    public PlatformRegistrar getRegistrar() {
        return registrationHelper;
    }

    @Override
    public void commonSetup(Runnable commonSetup) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent e) -> e.enqueueWork(commonSetup));
    }

    @Override
    public Object getEnergisingRecipeSubCategory() {
        return (Supplier<Supplier<SequencedAssemblySubCategory>>) () -> ForgeJeiEnergisingSubcategory::new;
    }

    
    @Override
    public void subscribeClientTickEnd(Runnable runnable) {
        addListener((Consumer<TickEvent.ClientTickEvent>) event -> {
            if (event.phase == TickEvent.Phase.END)
                runnable.run();
        });
    }
    
    @Override
    public void subscribeRenderLayerEvent(Consumer<RenderLayerEvent> consumer) {
        addListener(consumer);
    }

    @Override
    public void subscribeBeginFrameEvent(Consumer<BeginFrameEvent> consumer) {
        addListener(consumer);
    }

    @Override
    public void subscribeReloadRenderersEvent(Consumer<ReloadRenderersEvent> consumer) {
        addListener(consumer);
    }
    
    private void addListener(Consumer<? extends Event> consumer) {
        MinecraftForge.EVENT_BUS.addListener(consumer);
    }
}
