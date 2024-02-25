package org.antarcticgardens.cna;

import org.antarcticgardens.cna.fabric.FabricRegistrar;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

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
}
