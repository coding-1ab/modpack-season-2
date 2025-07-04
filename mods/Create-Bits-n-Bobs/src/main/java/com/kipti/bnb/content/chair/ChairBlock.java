package com.kipti.bnb.content.chair;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;

public class ChairBlock extends SeatBlock implements IWrenchable {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static BooleanProperty LEFT_ARM = BooleanProperty.create("left_arm");
    public static BooleanProperty RIGHT_ARM = BooleanProperty.create("right_arm");
    public static BooleanProperty CORNER = BooleanProperty.create("corner");

    public ChairBlock(Properties properties, DyeColor color) {
        super(properties, color);
        this.registerDefaultState(
            defaultBlockState()
                .setValue(LEFT_ARM, true)
                .setValue(RIGHT_ARM, true)
                .setValue(CORNER, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, LEFT_ARM, RIGHT_ARM, CORNER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        BlockState blockState = super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
        Direction facing = blockState.getValue(FACING);

        blockState = checkForObstructing(pLevel, pState, pCurrentPos, facing.getClockWise(), blockState, LEFT_ARM);
        blockState = checkForObstructing(pLevel, pState, pCurrentPos, facing.getCounterClockWise(), blockState, RIGHT_ARM);

        BlockState frontState = pLevel.getBlockState(pCurrentPos.relative(facing));
        blockState = blockState.setValue(CORNER,
            frontState.getBlock() instanceof ChairBlock &&
                frontState.getValue(FACING).getAxis() != pState.getValue(FACING).getAxis());

        return blockState;
    }

    private static @NotNull BlockState checkForObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
        blockState = blockState.setValue(property,
            !(sideState.getBlock() instanceof ChairBlock &&
                (sideState.getValue(FACING) == thisState.getValue(FACING) || sideState.getValue(FACING) == facing.getOpposite())));
        return blockState;
    }

}
