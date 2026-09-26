package org.antarcticgardens.cna.content.electricity.wire;

import net.minecraft.core.BlockPos;
import org.antarcticgardens.cna.util.HashSortedPair;

public record Wire(
        WireType type,
        HashSortedPair<BlockPos> points
) {
    public static Wire create(WireType type, BlockPos point1, BlockPos point2) {
        HashSortedPair<BlockPos> points = new HashSortedPair<>(point1, point2);
        return new Wire(type, points);
    }
}
