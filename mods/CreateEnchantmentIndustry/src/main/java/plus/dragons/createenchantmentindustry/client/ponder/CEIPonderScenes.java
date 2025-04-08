package plus.dragons.createenchantmentindustry.client.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
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

        HELPER.forComponents(CEIBlocks.PRINTER)
                .addStoryBoard("printer", MiscScene::printer, CEIPonderTags.EXPERIENCE_APPLIANCES);
    }

    public static void enchant(CreateSceneBuilder scene, ItemStack item, ResourceKey<Enchantment> enchantment, int level){
        if (DatagenModLoader.isRunningDataGen()) // scene.world().getHolderLookupProvider() cause null when get level
            return;
        var e = scene.world().getHolderLookupProvider()
                .lookup(Registries.ENCHANTMENT)
                .get().getOrThrow(enchantment);
        item.enchant(e,level);
    }
}
