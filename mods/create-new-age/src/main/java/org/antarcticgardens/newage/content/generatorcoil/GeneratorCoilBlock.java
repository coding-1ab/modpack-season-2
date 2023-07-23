package org.antarcticgardens.newage.content.generatorcoil;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.NewAgeBlockEntityTypes;

public class GeneratorCoilBlock extends RotatedPillarKineticBlock implements IBE<GeneratorCoilBlockEntity> {
    public GeneratorCoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Class<GeneratorCoilBlockEntity> getBlockEntityClass() {
        return GeneratorCoilBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GeneratorCoilBlockEntity> getBlockEntityType() {
        return NewAgeBlockEntityTypes.GENERATOR_COIL.get();
    }
}
