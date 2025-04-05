package plus.dragons.createenchantmentindustry.client.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.client.ponder.scene.CEIPonderTags;
import plus.dragons.createenchantmentindustry.client.ponder.scene.ExperienceScene;

public class CEIPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(AllItems.EXP_NUGGET, AllBlocks.EXPERIENCE_BLOCK)
                .addStoryBoard("experience/basic", ExperienceScene::basic, CEIPonderTags.EXPERIENCE_RELATED)
                .addStoryBoard("experience/advance", ExperienceScene::advance, CEIPonderTags.SUPER_EXPERIENCE_RELATED)
                .addStoryBoard("experience/prepare_for_super_enchant",ExperienceScene::prepare);
    }
}
