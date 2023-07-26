package org.antarcticgardens.newage.content.heat.stirlingengine;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class StirlingEngineBlock extends RotatedPillarKineticBlock implements IBE<StirlingEngineBlockEntity> {
    private final BlockEntityEntry<StirlingEngineBlockEntity> entry;

    public StirlingEngineBlock(Properties properties, BlockEntityEntry<StirlingEngineBlockEntity> entry) {
        super(properties);
        this.entry = entry;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Direction.Axis axis = Direction.Axis.X;
        if (context.getClickedFace().getAxis().isHorizontal()) {
            axis = context.getClickedFace().getAxis();
        } else if (context.getPlayer() != null) {
            Vector3f delta = new Vector3f((float) (context.getPlayer().getX() - context.getClickedPos().getX()), 0, (float) (context.getPlayer().getZ() - context.getClickedPos().getZ()));
            axis = Math.abs(delta.x) > Math.abs(delta.z) ? Direction.Axis.X : Direction.Axis.Z;
        }
        state = state.setValue(AXIS, axis);

        return state;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<StirlingEngineBlockEntity> getBlockEntityClass() {
        return StirlingEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StirlingEngineBlockEntity> getBlockEntityType() {
        return entry.get();
    }
}
