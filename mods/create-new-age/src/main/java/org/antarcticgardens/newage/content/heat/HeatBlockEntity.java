package org.antarcticgardens.newage.content.heat;

import net.minecraft.core.Direction;

public interface HeatBlockEntity {
    float getHeat();
    void addHeat(float amount);

    /**
     * @param from the side of block relative to the block accessing
     */
    default boolean canConnect(Direction from) {
        return true;
    }

    /**
     * @param from the side of block relative to the block accessing
     */
    default boolean canAdd(Direction from) {
        return canConnect(from);
    }

}
