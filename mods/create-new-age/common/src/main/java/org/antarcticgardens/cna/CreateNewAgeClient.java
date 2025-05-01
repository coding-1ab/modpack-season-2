package org.antarcticgardens.cna;

import net.createmod.ponder.foundation.PonderIndex;

public abstract class CreateNewAgeClient {
    protected void initialize() {
        PonderIndex.addPlugin(new CNAPonders());
    }
}
