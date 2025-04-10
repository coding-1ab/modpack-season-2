package plus.dragons.createenchantmentindustry.common.registry;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import plus.dragons.createdragonsplus.common.CDPRegistrate;
import plus.dragons.createdragonsplus.common.registrate.builder.CustomStatBuilder;
import plus.dragons.createenchantmentindustry.common.CEICommon;

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

public class CEIStats {
    public static final RegistryEntry<ResourceLocation,ResourceLocation> GRINDSTONE_EXPERIENCE_GRIND = create("grindstone_experience_grind")
            .lang("Mechanical Grindstone Experience Produce")
            .register();

    private static CustomStatBuilder<CDPRegistrate> create(String id){
        return REGISTRATE.customStat(id, ()->CEICommon.asResource(id));
    }

    public static void register(IEventBus modBus) {}
}
