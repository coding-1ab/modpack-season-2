package plus.dragons.createenchantmentindustry.client.ponder;

import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.client.ponder.scene.*;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class CEIPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(AllItems.EXP_NUGGET)
                .addStoryBoard("experience/basic", ExperienceScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("experience/advance", ExperienceScene::advance, CEIPonderTags.SUPER_EXPERIENCE_APPLIANCES)
                .addStoryBoard("experience/prepare_for_super_enchant",ExperienceScene::prepare);


        HELPER.forComponents(CEIBlocks.EXPERIENCE_HATCH)
                .addStoryBoard("experience_hatch", MiscScene::experienceHatch, CEIPonderTags.EXPERIENCE_APPLIANCES);

        HELPER.forComponents(CEIBlocks.MECHANICAL_GRINDSTONE)
                .addStoryBoard("grindstone/basic", GrindstoneScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("grindstone/extra", GrindstoneScene::extra);

        HELPER.forComponents(CEIBlocks.BLAZE_ENCHANTER)
                .addStoryBoard("enchanter", EnchanterScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("enchanter", EnchanterScene::superEnchant, CEIPonderTags.SUPER_EXPERIENCE_APPLIANCES);

        HELPER.forComponents(CEIBlocks.BLAZE_FORGER)
                .addStoryBoard("forger", ForgerScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("forger", ForgerScene::superEnchant, CEIPonderTags.SUPER_EXPERIENCE_APPLIANCES);
    }
}
