package org.antarcticgardens.newage.content.energiser;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.antarcticgardens.newage.tools.RegistryTool;
import org.jetbrains.annotations.NotNull;

public class EnergiserBlock extends HorizontalKineticBlock implements IBE<EnergiserBlockEntity> {

    public static EnergiserBlock self;
    public EnergiserBlock(int level) {
        super(Properties.copy(Blocks.ANDESITE).noOcclusion());
        self = this;

        RegistryTool.registerAssemblyBlock("energiser" + level, this);
        EnergiserBlockEntity.type = RegistryTool.registerBlockEntityBlock("energiser" + level, this,
                (blockPos, blockState) -> new EnergiserBlockEntity(blockPos, blockState, level));
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, LevelReader worldIn, BlockPos pos) {
        return !AllBlocks.BASIN.has(worldIn.getBlockState(pos.below()));
    }

    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (context instanceof EntityCollisionContext
                && ((EntityCollisionContext) context).getEntity() instanceof Player)
            return AllShapes.CASING_14PX.get(Direction.DOWN);

        return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction prefferedSide = getPreferredHorizontalFacing(context);
        if (prefferedSide != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, prefferedSide);
        return super.getStateForPlacement(context);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    public Class<EnergiserBlockEntity> getBlockEntityClass() {
        return EnergiserBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnergiserBlockEntity> getBlockEntityType() {
        return EnergiserBlockEntity.type;
    }
}
