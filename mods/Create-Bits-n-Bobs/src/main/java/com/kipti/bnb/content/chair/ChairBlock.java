package com.kipti.bnb.content.chair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;

public class ChairBlock extends SeatBlock implements IWrenchable {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static BooleanProperty LEFT_ARM = BooleanProperty.create("left_arm");
    public static BooleanProperty RIGHT_ARM = BooleanProperty.create("right_arm");
    public static BooleanProperty BACK_FLAT = BooleanProperty.create("back_flat");
    public static BooleanProperty CORNER = BooleanProperty.create("corner");

    public ChairBlock(Properties properties, DyeColor color) {
        super(properties, color);
        this.registerDefaultState(
            defaultBlockState()
                .setValue(LEFT_ARM, true)
                .setValue(RIGHT_ARM, true)
                .setValue(CORNER, false)
                .setValue(BACK_FLAT, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, LEFT_ARM, RIGHT_ARM, CORNER, BACK_FLAT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return calculateShape(super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection().getOpposite()), pContext.getLevel(), pContext.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        BlockState blockState = super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
        return calculateShape(blockState, pLevel, pCurrentPos);
    }

    private @NotNull BlockState calculateShape(BlockState blockState, LevelAccessor pLevel, BlockPos pCurrentPos) {
        Direction facing = blockState.getValue(FACING);

        BlockState stateLeft = pLevel.getBlockState(pCurrentPos.relative(facing.getClockWise()));
        boolean hasConnectableLeft = AllBlocks.SEATS.contains(stateLeft.getBlock()) || stateLeft.getBlock() instanceof ChairBlock &&
            (stateLeft.getValue(FACING) == facing || stateLeft.getValue(FACING) == facing.getCounterClockWise());

        BlockState stateRight = pLevel.getBlockState(pCurrentPos.relative(facing.getCounterClockWise()));
        boolean hasConnectableRight = AllBlocks.SEATS.contains(stateRight.getBlock()) || stateRight.getBlock() instanceof ChairBlock &&
            (stateRight.getValue(FACING) == facing || stateRight.getValue(FACING) == facing.getClockWise());

        BlockState stateFront = pLevel.getBlockState(pCurrentPos.relative(facing));
        boolean hasConnectableFront = stateFront.getBlock() instanceof ChairBlock &&
            (stateFront.getValue(FACING) == facing.getCounterClockWise() || stateFront.getValue(FACING) == facing.getClockWise());

        BlockState stateBack = pLevel.getBlockState(pCurrentPos.relative(facing.getOpposite()));
//        boolean isFlatBack = stateBack.getBlock() instanceof ChairBlock &&
//            (stateBack.getValue(FACING) == facing.getOpposite() || stateBack.getValue(CORNER) && stateBack.getValue(FACING) != facing);

        boolean isFlatBack = stateBack.isCollisionShapeFullBlock(pLevel, pCurrentPos.relative(facing.getOpposite()));

        boolean corner = hasConnectableFront && (hasConnectableLeft != hasConnectableRight);
        if (hasConnectableFront) {
            Direction cornerFacing = hasConnectableLeft ? facing.getCounterClockWise() : facing.getClockWise();
            BlockPos cornerPos = pCurrentPos.relative(cornerFacing);
            BlockState stateBackCorner = pLevel.getBlockState(cornerPos);
            isFlatBack = isFlatBack || stateBackCorner.isCollisionShapeFullBlock(pLevel, cornerPos);
        }

        return blockState
            .setValue(LEFT_ARM, !hasConnectableLeft)
            .setValue(RIGHT_ARM, !hasConnectableRight)
            .setValue(CORNER, corner)
            .setValue(BACK_FLAT, isFlatBack);
    }

    private static boolean getFlatObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
        return (sideState.getBlock() instanceof ChairBlock &&
            (sideState.getValue(FACING) == facing));
    }

//    private static boolean getFlatObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
//        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
//        return (sideState.getBlock() instanceof ChairBlock &&
//            (sideState.getValue(FACING) == facing));
//    }
//
//    private static @NotNull BlockState checkForFlatObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
//        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
//        blockState = blockState.setValue(property, (sideState.getBlock() instanceof ChairBlock && (sideState.getValue(FACING) == facing)));
//        return blockState;
//    }
//
//    private static @NotNull BlockState checkForObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
//        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
//        blockState = blockState.setValue(property,
//            !(sideState.getBlock() instanceof ChairBlock && (sideState.getValue(FACING) == thisState.getValue(FACING) || sideState.getValue(FACING) == facing.getOpposite())));
//        return blockState;
//    }

}
