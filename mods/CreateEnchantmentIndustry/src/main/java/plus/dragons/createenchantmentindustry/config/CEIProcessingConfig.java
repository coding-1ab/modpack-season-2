package plus.dragons.createenchantmentindustry.config;

import net.createmod.catnip.config.ConfigBase;

public class CEIProcessingConfig extends ConfigBase {
    public final ConfigFloat regularLightningStrikeTransformXpBlockChance = f(1, 0, 1, "regularLightningStrikeTransformXpBlockChance", CEIProcessingConfig.Comments.regularLightningStrikeTransformXpBlockChance);

    @Override
    public String getName() {
        return "processing";
    }

    static class Comments {
        static final String regularLightningStrikeTransformXpBlockChance = "Probability of regular lightning strike transforming Block of Experience.";
    }
}
