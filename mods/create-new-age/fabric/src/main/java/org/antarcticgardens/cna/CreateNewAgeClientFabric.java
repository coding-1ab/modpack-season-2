package org.antarcticgardens.cna;

import net.fabricmc.api.ClientModInitializer;

public class CreateNewAgeClientFabric extends CreateNewAgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        this.initialize();
    }
}
