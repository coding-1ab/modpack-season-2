package org.antarcticgardens.newage.content.heat;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface HeatBlockEntity {
    float getHeat();
    void addHeat(float amount);

    void setHeat(float amount);

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
    static <T extends  BlockEntity & HeatBlockEntity> void transferAround(T self) {
        if (self.getLevel() == null) {
            return;
        }
        float totalToAverage = self.getHeat();
        int totalBlocks = 1;
        HeatBlockEntity[] setters = new HeatBlockEntity[6];
        for (int i = 0 ; i < 6 ; i++) {
            Direction value = Direction.values()[i];
            BlockEntity entity = self.getLevel().getBlockEntity(self.getBlockPos().relative(value));
            if (entity instanceof HeatBlockEntity hbe && hbe.canAdd(value)) {
                setters[i] = hbe;
                totalToAverage += hbe.getHeat();
                totalBlocks++;
            }
        }
        average(self, totalToAverage, totalBlocks, setters);
    }

    static <T extends BlockEntity & HeatBlockEntity> void average(T self, float totalToAverage, int totalBlocks, HeatBlockEntity[] setters) {
        float setAmount = totalToAverage / totalBlocks;
        self.setHeat(setAmount);
        int i = 0;
        for (HeatBlockEntity hbe : setters) {
            if (hbe != null) {
                hbe.setHeat(setAmount);
            }
            i++;
        }
    }

}
