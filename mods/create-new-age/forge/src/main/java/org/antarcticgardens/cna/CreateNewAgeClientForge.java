package org.antarcticgardens.cna;

import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateNewAgeClientForge extends CreateNewAgeClient {
    public void onClientSetup(final FMLClientSetupEvent event) {
        this.initialize();
        
        ModContainer modContainer = ModList.get()
                .getModContainerById(CreateNewAge.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("What the..."));

        modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, previousScreen) -> new BaseConfigScreen(previousScreen, CreateNewAge.MOD_ID)));
    }
}
