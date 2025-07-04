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

public class ChairBlock extends SeatBlock implements IWrenchable {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static BooleanProperty LEFT_ARM = BooleanProperty.create("left_arm");
    public static BooleanProperty RIGHT_ARM = BooleanProperty.create("right_arm");

    public ChairBlock(Properties properties, DyeColor color) {
        super(properties, color);
        this.registerDefaultState(defaultBlockState()
            .setValue(LEFT_ARM, true)
            .setValue(RIGHT_ARM, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, LEFT_ARM, RIGHT_ARM);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        BlockState blockState = super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
        Direction facing = blockState.getValue(FACING);
        BlockState leftState = pLevel.getBlockState(pCurrentPos.relative(facing.getClockWise()));
        blockState = blockState.setValue(LEFT_ARM, !(leftState.getBlock() instanceof ChairBlock && leftState.getValue(FACING) == facing));
        BlockState rightState = pLevel.getBlockState(pCurrentPos.relative(facing.getCounterClockWise()));
        blockState = blockState.setValue(RIGHT_ARM, !(rightState.getBlock() instanceof ChairBlock && rightState.getValue(FACING) == facing));
        return blockState;
    }

}
