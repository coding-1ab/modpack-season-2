package org.antarcticgardens.newage.content.electricity.battery;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.NewAgeBlockEntityTypes;

public class BatteryControllerBlock extends Block implements IBE<BatteryControllerBlockEntity> {
    public BatteryControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<BatteryControllerBlockEntity> getBlockEntityClass() {
        return BatteryControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BatteryControllerBlockEntity> getBlockEntityType() {
        return NewAgeBlockEntityTypes.BATTERY_CONTROLLER.get();
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<S> p_153214_) {
        return (world, pos, state, be) -> {
            if (be instanceof BatteryControllerBlockEntity controller)
                controller.tick();
        };
    }
}
