package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class GirderStrutBlock extends Block implements IBE<GirderStrutBlockEntity>, SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final int MAX_SPAN = 8;

    private StrutModelType modelType;

    public GirderStrutBlock(final Properties properties, final StrutModelType modelType) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(WATERLOGGED, false));
        this.modelType = modelType;
    }

    public static NonNullFunction<Properties, GirderStrutBlock> normal() {
        return properties -> new GirderStrutBlock(properties, StrutModelType.NORMAL);
    }

    public static NonNullFunction<Properties, GirderStrutBlock> weathered() {
        return properties -> new GirderStrutBlock(properties, StrutModelType.WEATHERED);
    }

    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState neighbourState, final LevelAccessor world,
                                  final BlockPos pos, final BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED))
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return state;
    }

    @Override
    public FluidState getFluidState(final BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final FluidState ifluidstate = level.getFluidState(pos);
        final BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        return state.setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return BnbShapes.GIRDER_STRUT.get(state.getValue(FACING));
    }

    @Override
    public PushReaction getPistonPushReaction(final BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                final BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof final GirderStrutBlockEntity self) {
                    for (BlockPos otherPos : self.getConnectionsCopy()) {
                        otherPos = otherPos.offset(pos);
                        final BlockEntity otherBe = level.getBlockEntity(otherPos);
                        if (otherBe instanceof final GirderStrutBlockEntity other) {
                            other.removeConnection(pos);
                            if (other.connectionCount() == 0) {
                                level.destroyBlock(otherPos, true);
                            }
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Class<GirderStrutBlockEntity> getBlockEntityClass() {
        return GirderStrutBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GirderStrutBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GIRDER_STRUT.get();
    }

    public StrutModelType getModelType() {
        return modelType;
    }

    public void setModelType(final StrutModelType modelType) {
        this.modelType = modelType;
    }
}
