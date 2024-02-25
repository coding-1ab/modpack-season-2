package org.antarcticgardens.cna;

import org.antarcticgardens.cna.platform.PlatformRegistrar;

public abstract class Platform {
    public abstract PlatformRegistrar getRegistrar();
    public abstract void commonSetup(Runnable commonSetup);
}
