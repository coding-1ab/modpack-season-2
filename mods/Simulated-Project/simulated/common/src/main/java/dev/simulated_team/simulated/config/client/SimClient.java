package dev.simulated_team.simulated.config.client;

import dev.simulated_team.simulated.config.client.items.SimItemConfigs;
import net.createmod.catnip.config.ConfigBase;

public class SimClient extends ConfigBase {
    public final SimItemConfigs itemConfig = this.nested(0, SimItemConfigs::new, SimClient.Comments.itemConfig);

    @Override
    public String getName() {
        return "client";
    }

    private static class Comments {
        static String itemConfig = "Settings of Simulated Items";
    }
}
