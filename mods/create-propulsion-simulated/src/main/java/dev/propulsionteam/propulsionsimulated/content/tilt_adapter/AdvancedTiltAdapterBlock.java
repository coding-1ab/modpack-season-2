package dev.propulsionteam.propulsionsimulated.content.tilt_adapter;

import javax.annotation.Nullable;

import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AdvancedTiltAdapterBlock extends AbstractTiltAdapterBlock<AdvancedTiltAdapterBlockEntity> implements IWrenchable {

    public AdvancedTiltAdapterBlock(Properties properties) {
        super(properties, AdvancedTiltAdapterBlockEntity.class,
            PropulsionBlockEntities.ADVANCED_TILT_ADAPTER_BLOCK_ENTITY::get,
            AdvancedTiltAdapterBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(@Nullable BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, @Nullable CollisionContext context) {
        Direction direction = getDirection(state);
        if (direction.getAxis() == Axis.Y) {
            direction = direction.getOpposite();
        }
        return PropulsionShapes.ADVANCED_TILT_ADAPTER.get(direction);
    }
}
