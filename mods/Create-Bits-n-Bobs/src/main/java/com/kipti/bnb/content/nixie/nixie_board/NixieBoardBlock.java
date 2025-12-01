package com.kipti.bnb.content.nixie.nixie_board;

import com.kipti.bnb.content.nixie.foundation.*;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NixieBoardBlock extends DoubleOrientedDisplayBlock implements IBE<GenericNixieDisplayBlockEntity>, IWrenchable, DyeProviderBlock, IGenericNixieDisplayBlock {

    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    final @Nullable DyeColor dyeColor;

    public NixieBoardBlock(Properties p_52591_, @Nullable DyeColor dyeColor) {
        super(p_52591_);
        this.dyeColor = dyeColor;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return BnbBlocks.NIXIE_BOARD.asItem().getDefaultInstance();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        Direction left = DoubleOrientedBlockModel.getLeft(state);
        Direction below = state.getValue(FACING).getOpposite();

        state = state
                .setValue(LEFT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, context.getLevel().getBlockState(context.getClickedPos().relative(left))))
                .setValue(RIGHT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, context.getLevel().getBlockState(context.getClickedPos().relative(left.getOpposite()))))
                .setValue(BOTTOM, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, context.getLevel().getBlockState(context.getClickedPos().relative(below))))
                .setValue(LIT, false);
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT, RIGHT, BOTTOM);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction left = DoubleOrientedBlockModel.getLeft(state);
        Direction right = left.getOpposite();
        Direction bottom = state.getValue(FACING).getOpposite();

        if (direction == left) {
            return state.setValue(LEFT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, neighborState));
        } else if (direction == right) {
            return state.setValue(RIGHT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, neighborState));
        } else if (direction == bottom) {
            return state.setValue(BOTTOM, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, neighborState));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof DyeItem dyeItem && dyeItem.getDyeColor() != dyeColor) {
            if (!level.isClientSide) {
                GenericNixieDisplayBlockEntity be = (GenericNixieDisplayBlockEntity) level.getBlockEntity(pos);
                be.applyToEachElementOfThisStructure((display) -> {
                    DyeColor newColor = dyeItem.getDyeColor();
                    BlockState newState = BnbBlocks.DYED_NIXIE_BOARD.get(newColor).getDefaultState()
                            .setValue(FACING, display.getBlockState().getValue(FACING))
                            .setValue(ORIENTATION, display.getBlockState().getValue(ORIENTATION))
                            .setValue(LEFT, display.getBlockState().getValue(LEFT))
                            .setValue(RIGHT, display.getBlockState().getValue(RIGHT))
                            .setValue(BOTTOM, display.getBlockState().getValue(BOTTOM))
                            .setValue(LIT, display.getBlockState().getValue(LIT));
                    level.setBlockAndUpdate(display.getBlockPos(), newState);
                    GenericNixieDisplayBlockEntity newBe = (GenericNixieDisplayBlockEntity) level.getBlockEntity(display.getBlockPos());
                    newBe.inheritDataFrom(display);
                });
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        final Direction frontTarget = DoubleOrientedBlockModel.getFront(state.getValue(FACING), state.getValue(ORIENTATION));
        final boolean isFront = frontTarget.getAxis() == state.getValue(ORIENTATION).getAxis();
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

    @Override
    public List<GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions> getPossibleDisplayOptions() {
        return List.of(GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.NONE, GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.DOUBLE_CHAR, GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES);
    }
}
