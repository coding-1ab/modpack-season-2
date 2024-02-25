package org.antarcticgardens.cna.content.nuclear.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.cna.config.CNAConfig;
import org.antarcticgardens.cna.content.nuclear.reactor.rod.ReactorRodBlockEntity;

import java.util.List;

public class RodFindingReactorBlockEntity extends BlockEntity {
    public RodFindingReactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void findRods(List<ReactorRodBlockEntity> list, Direction dir) {
        if (level == null)
            return;

        BlockEntity entity = level.getBlockEntity(getBlockPos().relative(dir));

        int c = 0;
        while (entity instanceof ReactorRodBlockEntity rrbe) {
            c++;
            if (c > CNAConfig.getCommon().maxRodsInDirection.get()) {
                return;
            }
            list.add(rrbe);
            entity = level.getBlockEntity(rrbe.getBlockPos().relative(dir));
        }
    }

}
