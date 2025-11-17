package com.kipti.bnb;

import com.kipti.bnb.foundation.ponder.BnbPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreateBitsnBobs.MOD_ID, dist = Dist.CLIENT)
public class CreateBitsnBobsClient {

    public CreateBitsnBobsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        PonderIndex.addPlugin(new BnbPonderPlugin());
    }

}