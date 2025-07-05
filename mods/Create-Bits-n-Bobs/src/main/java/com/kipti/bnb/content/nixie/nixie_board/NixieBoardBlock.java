package com.kipti.bnb.content.nixie.nixie_board;

import com.kipti.bnb.content.nixie.foundation.DoubleOrientedBlock;
import com.kipti.bnb.content.nixie.foundation.DoubleOrientedBlockModel;
import com.kipti.bnb.content.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NixieBoardBlock extends DoubleOrientedBlock implements IBE<GenericNixieDisplayBlockEntity>, IWrenchable {

    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    final @Nullable DyeColor dyeColor;

    public NixieBoardBlock(Properties p_52591_, @Nullable DyeColor dyeColor) {
        super(p_52591_);
        this.dyeColor = dyeColor;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        Direction facing = state.getValue(FACING);
        Direction orientation = state.getValue(ORIENTATION);
        Direction left = DoubleOrientedBlockModel.getLeft(facing, orientation);
        state = state
            .setValue(LEFT, context.getLevel().getBlockState(context.getClickedPos().relative(left.getOpposite())).is(this))
            .setValue(RIGHT, context.getLevel().getBlockState(context.getClickedPos().relative(left)).is(this))
            .setValue(LIT, false);
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT, RIGHT);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        Direction orientation = state.getValue(ORIENTATION);
        Direction left = DoubleOrientedBlockModel.getLeft(facing, orientation);
        Direction right = left.getOpposite();

        if (direction == left.getOpposite()) {
            return state.setValue(LEFT, neighborState.is(this));
        } else if (direction == right.getOpposite()) {
            return state.setValue(RIGHT, neighborState.is(this));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof DyeItem dyeItem && dyeItem.getDyeColor() != dyeColor) {
            if (!level.isClientSide) {
                DyeColor newColor = dyeItem.getDyeColor();
                BlockState newState = BnbBlocks.DYED_NIXIE_BOARD.get(newColor).getDefaultState()
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(ORIENTATION, state.getValue(ORIENTATION))
                    .setValue(LEFT, state.getValue(LEFT))
                    .setValue(RIGHT, state.getValue(RIGHT))
                    .setValue(LIT, state.getValue(LIT));
                level.setBlockAndUpdate(pos, newState);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (heldItem.getItem() instanceof NameTagItem) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GenericNixieDisplayBlockEntity display) {
                String name = heldItem.getHoverName().getString();
                display.findControllerBlockEntity().applyTextToDisplay(name);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction frontTarget = DoubleOrientedBlockModel.getFront(state.getValue(FACING), state.getValue(ORIENTATION));
        boolean isFront = frontTarget.getAxis() == state.getValue(ORIENTATION).getAxis();
        return isFront ? BnbShapes.NIXIE_BOARD_SIDE.get(state.getValue(FACING))
            : BnbShapes.NIXIE_BOARD_FRONT.get(state.getValue(FACING));
    }

    @Override
    public Class<GenericNixieDisplayBlockEntity> getBlockEntityClass() {
        return GenericNixieDisplayBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GenericNixieDisplayBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GENERIC_NIXIE_DISPLAY.get();
    }

    public @Nullable DyeColor getDyeColor() {
        return dyeColor;
    }

}
