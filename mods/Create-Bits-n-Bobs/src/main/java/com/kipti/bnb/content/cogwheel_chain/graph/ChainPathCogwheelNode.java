package com.kipti.bnb.content.cogwheel_chain.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record ChainPathCogwheelNode(int side, BlockPos offsetFromStart) {
    public void write(CompoundTag posTag) {
        posTag.putInt("Side", side);
        posTag.putInt("OffsetX", offsetFromStart.getX());
        posTag.putInt("OffsetY", offsetFromStart.getY());
        posTag.putInt("OffsetZ", offsetFromStart.getZ());
    }

    public static ChainPathCogwheelNode read(CompoundTag posTag) {
        int side = posTag.getInt("Side");
        BlockPos offset = new BlockPos(
            posTag.getInt("OffsetX"),
            posTag.getInt("OffsetY"),
            posTag.getInt("OffsetZ")
        );
        return new ChainPathCogwheelNode(side, offset);
    }
}
