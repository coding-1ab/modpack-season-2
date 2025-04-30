package plus.dragons.createenchantmentindustry.common.registry;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.bus.api.IEventBus;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMountedFluidStorageType;

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

public class CEIMountedStorageTypes {
    public static final RegistryEntry<MountedFluidStorageType<?>, ExperienceLanternMountedFluidStorageType> EXPERIENCE_LANTERN = REGISTRATE
            .mountedFluidStorage("experience_lantern",ExperienceLanternMountedFluidStorageType::new)
            .register();

    public static void register(IEventBus modBus) {}
}
