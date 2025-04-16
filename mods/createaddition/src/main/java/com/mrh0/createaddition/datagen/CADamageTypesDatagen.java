package com.mrh0.createaddition.datagen;

import com.mrh0.createaddition.index.CADamageTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class CADamageTypesDatagen {
    public static void bootstrap(BootstrapContext<DamageType> bs) {
        bs.register(CADamageTypes.BARBED_WIRE_KEY,new DamageType(
                CADamageTypes.BARBED_WIRE_KEY.location().toLanguageKey(),
                DamageScaling.NEVER,
                0.1f
                ));
        bs.register(CADamageTypes.TESLA_COIL_KEY,new DamageType(
                CADamageTypes.TESLA_COIL_KEY.location().toLanguageKey(),
                DamageScaling.NEVER,
                0.1f
        ));
    }
}
