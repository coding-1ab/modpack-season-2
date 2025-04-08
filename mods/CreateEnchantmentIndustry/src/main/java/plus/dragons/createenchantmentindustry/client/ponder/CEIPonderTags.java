package plus.dragons.createenchantmentindustry.client.ponder;

import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class CEIPonderTags {
    public static final ResourceLocation EXPERIENCE_APPLIANCES = CEICommon.asResource("experience_appliances");
    public static final ResourceLocation SUPER_EXPERIENCE_APPLIANCES = CEICommon.asResource("super_experience_related");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        helper.registerTag(EXPERIENCE_APPLIANCES)
                .addToIndex()
                .item(AllBlocks.EXPERIENCE_BLOCK.get(), true, false)
                .title("Experience Related")
                .description("Components which will be used when processing and applying Experience")
                .register();

        helper.registerTag(SUPER_EXPERIENCE_APPLIANCES)
                .addToIndex()
                .item(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get(), true, false)
                .title("Super Experience Related")
                .description("Components which will be used when processing and applying Super Experience")
                .register();

        HELPER.addToTag(EXPERIENCE_APPLIANCES)
                .add(AllBlocks.ITEM_DRAIN)
                .add(AllBlocks.SPOUT)
                .add(CEIBlocks.MECHANICAL_GRINDSTONE)
                .add(CDPBlockEntities.FLUID_HATCH)
                .add(CEIBlocks.EXPERIENCE_HATCH)
                .add(CEIBlocks.BLAZE_ENCHANTER)
                .add(CEIBlocks.BLAZE_FORGER)
                .add(CEIBlocks.PRINTER);

        HELPER.addToTag(SUPER_EXPERIENCE_APPLIANCES)
                .add(CEIBlocks.BLAZE_ENCHANTER)
                .add(CEIBlocks.BLAZE_FORGER);
    }
}
