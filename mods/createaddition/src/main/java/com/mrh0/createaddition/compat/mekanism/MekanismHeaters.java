package com.mrh0.createaddition.compat.mekanism;

import com.simibubi.create.api.boiler.BoilerHeater;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MekanismHeaters {
    public static void registerHeaters() {
        BoilerHeater.REGISTRY.register(MekanismBlocks.RESISTIVE_HEATER.get(), (level, pos, state) -> {
            return -1;
        });
    }
}
