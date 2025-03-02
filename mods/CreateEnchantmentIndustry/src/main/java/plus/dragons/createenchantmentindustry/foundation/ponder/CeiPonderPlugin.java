package plus.dragons.createenchantmentindustry.foundation.ponder;

import net.createmod.ponder.api.registration.*;
import net.minecraft.resources.ResourceLocation;

public class CeiPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return "create_enchantment_industry";
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CeiPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        CeiPonderTags.register(helper);
    }
}
