package plus.dragons.createenchantmentindustry.client.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createdragonsplus.common.registry.CDPBlockEntities;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

public class CEIPonderTags {
    public static final ResourceLocation EXPERIENCE_RELATED = CEICommon.asResource("experience_related");
    public static final ResourceLocation SUPER_EXPERIENCE_RELATED = CEICommon.asResource("super_experience_related");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        helper.registerTag(EXPERIENCE_RELATED)
                .addToIndex()
                .item(AllBlocks.EXPERIENCE_BLOCK.get(), true, false)
                .title("Experience Related")
                .description("Things that will be used when processing and applying Experience")
                .register();

        helper.registerTag(SUPER_EXPERIENCE_RELATED)
                .addToIndex()
                .item(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get(), true, false)
                .title("Super Experience Related")
                .description("Things that will be used when processing and applying Super Experience")
                .register();

        HELPER.addToTag(EXPERIENCE_RELATED)
                .add(AllItems.EXP_NUGGET)
                .add(AllBlocks.EXPERIENCE_BLOCK)
                .add(AllBlocks.ITEM_DRAIN)
                .add(AllBlocks.SPOUT)
                .add(CEIBlocks.MECHANICAL_GRINDSTONE)
                .add(CDPBlockEntities.FLUID_HATCH)
                .add(CEIBlocks.EXPERIENCE_HATCH)
                .add(CEIBlocks.BLAZE_ENCHANTER)
                .add(CEIBlocks.BLAZE_FORGER)
                .add(CEIBlocks.PRINTER)
                .add(CEIItems.ENCHANTING_TEMPLATE);

        HELPER.addToTag(SUPER_EXPERIENCE_RELATED)
                .add(CEIItems.SUPER_EXPERIENCE_NUGGET)
                .add(AllBlocks.EXPERIENCE_BLOCK)
                .add(CEIBlocks.SUPER_EXPERIENCE_BLOCK)
                .add(CEIItems.EXPERIENCE_CAKE)
                .add(CEIBlocks.BLAZE_ENCHANTER)
                .add(CEIBlocks.BLAZE_FORGER)
                .add(CEIItems.SUPER_ENCHANTING_TEMPLATE);
    }
}
