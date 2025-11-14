package com.kipti.bnb.content.cogwheel_chain.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record ChainPathCogwheelNode(float sideFactor, BlockPos offsetFromStart) {
    public void write(CompoundTag posTag) {
        posTag.putFloat("Side", sideFactor);
        posTag.putInt("OffsetX", offsetFromStart.getX());
        posTag.putInt("OffsetY", offsetFromStart.getY());
        posTag.putInt("OffsetZ", offsetFromStart.getZ());
    }

    public static ChainPathCogwheelNode read(CompoundTag posTag) {
        float side = posTag.getFloat("Side");
        BlockPos offset = new BlockPos(
            posTag.getInt("OffsetX"),
            posTag.getInt("OffsetY"),
            posTag.getInt("OffsetZ")
        );
        return new ChainPathCogwheelNode(side, offset);
    }
}
