package org.antarcticgardens.cna;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.antarcticgardens.cna.content.electricity.network.NetworkTicker;
import org.antarcticgardens.cna.fabric.CNAFabricBiomeModifiers;

public class CreateNewAgeFabric extends CreateNewAge implements ModInitializer {
    @Override
    public void onInitialize() {
        this.initialize(new FabricPlatform());
        CNAFabricBiomeModifiers.bootstrap();
        ServerTickEvents.END_WORLD_TICK.register(NetworkTicker::tickWorld);
    }
}
