package org.antarcticgardens.cna.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.antarcticgardens.cna.CreateNewAge;

public class CNAConfig {
    private static final CNAConfig INSTANCE = new CNAConfig();

    private final ClientConfig client;
    private final CommonConfig common;

    private CNAConfig() {
        var client = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        this.client = client.getLeft();
        CreateNewAge.getInstance().getPlatform().getRegistrar().registerConfig(ModConfig.Type.CLIENT, client.getRight());

        var common = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
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
