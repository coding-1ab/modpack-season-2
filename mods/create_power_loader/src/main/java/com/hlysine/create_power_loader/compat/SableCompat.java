package com.hlysine.create_power_loader.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

public class SableCompat {
    public static BlockPos projectOutOfSubLevel(Level level, BlockPos blockPos) {
        if (SableCompanion.INSTANCE.getContaining(level, blockPos.getCenter()) != null) {
            return BlockPos.containing(SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) blockPos.getCenter()));
        }
        return blockPos;
    }
}
