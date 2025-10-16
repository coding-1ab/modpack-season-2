package org.antarcticgardens.cna.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.antarcticgardens.cna.CreateNewAge;

public class CNAConfig {
    private static final CNAConfig INSTANCE = new CNAConfig();

    private final ClientConfig client;
    private final CommonConfig common;

    private CNAConfig() {
        var client = new ModConfigSpec.Builder().configure(ClientConfig::new);
        this.client = client.getLeft();
        CreateNewAge.getInstance().getPlatform().getRegistrar().registerConfig(ModConfig.Type.CLIENT, client.getRight());

        var common = new ModConfigSpec.Builder().configure(CommonConfig::new);
        this.common = common.getLeft();
        CreateNewAge.getInstance().getPlatform().getRegistrar().registerConfig(ModConfig.Type.COMMON, common.getRight());
    }

    public static ClientConfig getClient() {
        return INSTANCE.client;
    }

    public static CommonConfig getCommon() {
        return INSTANCE.common;
    }
    
    public static void load() {  }
}
