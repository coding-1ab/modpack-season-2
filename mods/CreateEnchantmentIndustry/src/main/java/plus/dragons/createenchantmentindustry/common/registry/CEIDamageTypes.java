package plus.dragons.createenchantmentindustry.common.registry;

import com.simibubi.create.foundation.damageTypes.DamageTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class CEIDamageTypes {
    public static final ResourceKey<DamageType>
            GRIND = key("grind");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CEICommon.asResource(name));
    }

    public static void bootstrap(BootstrapContext<DamageType> ctx) {
        new DamageTypeBuilder(GRIND).scaling(DamageScaling.ALWAYS).register(ctx);
    }
}
