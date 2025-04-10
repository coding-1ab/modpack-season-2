package plus.dragons.createenchantmentindustry.common.registry;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import plus.dragons.createdragonsplus.common.CDPRegistrate;
import plus.dragons.createdragonsplus.common.registrate.builder.CustomStatBuilder;
import plus.dragons.createenchantmentindustry.common.CEICommon;

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

public class CEIStats {
    public static final RegistryEntry<ResourceLocation,ResourceLocation> GRINDSTONE_EXPERIENCE = create("mechanical_grindstone_experience")
            .lang("Experience Produced (by Mechanical Grindstone)")
            .register();

    public static final RegistryEntry<ResourceLocation,ResourceLocation> SUPER_ENCHANT = create("super_enchant")
            .lang("Super-Enchant")
            .register();

    public static final RegistryEntry<ResourceLocation,ResourceLocation> PRINT = create("print")
            .lang("Printer Used")
            .register();

    public static final RegistryEntry<ResourceLocation,ResourceLocation> FORGE = create("forge")
            .lang("Blaze Forger Used")
            .register();

    public static final RegistryEntry<ResourceLocation,ResourceLocation> ENCHANT = create("enchant")
            .lang("Blaze Enchanter Used")
            .register();

    private static CustomStatBuilder<CDPRegistrate> create(String id){
        return REGISTRATE.customStat(id, ()->CEICommon.asResource(id));
    }

    public static void register(IEventBus modBus) {}
}
