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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GirderStrutBlock extends Block implements IBE<GirderStrutBlockEntity> {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final int MAX_SPAN = 8;

    StrutModelType modelType;

    public GirderStrutBlock(Properties properties, StrutModelType modelType) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
        this.modelType = modelType;
    }

    public static NonNullFunction<Properties, GirderStrutBlock> normal() {
        return properties -> new GirderStrutBlock(properties, StrutModelType.NORMAL);
    }

    public static NonNullFunction<Properties, GirderStrutBlock> weathered() {
        return properties -> new GirderStrutBlock(properties, StrutModelType.WEATHERED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BnbShapes.GIRDER_STRUT.get(state.getValue(FACING));
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide) return;
        BlockEntity selfBe = level.getBlockEntity(pos);
        if (!(selfBe instanceof GirderStrutBlockEntity self)) return;
        //TODO idk what this is for so remove it later

    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof GirderStrutBlockEntity self) {
                    for (BlockPos otherPos : self.getConnectionsCopy()) {
                        otherPos = otherPos.offset(pos);
                        BlockEntity otherBe = level.getBlockEntity(otherPos);
                        if (otherBe instanceof GirderStrutBlockEntity other) {
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

}
