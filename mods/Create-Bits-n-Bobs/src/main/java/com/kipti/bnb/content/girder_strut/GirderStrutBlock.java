package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbBlockEntities;
import com.simibubi.create.foundation.block.IBE;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GirderStrutBlock extends Block implements IBE<GirderStrutBlockEntity> {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Shapes.box(4 / 16d, 0, 4 / 16d, 12 / 16d, 12 / 16d, 12 / 16d);
    private static final int MAX_SPAN = 6;

    public GirderStrutBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
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
        return SHAPE;
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

        // Scan partner struts and link them, for now, in future the item directly presses it

        for (int x = -MAX_SPAN; x <= MAX_SPAN; x++) {
            for (int y = -MAX_SPAN; y <= MAX_SPAN; y++) {
                for (int z = -MAX_SPAN; z <= MAX_SPAN; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos scan = pos.offset(x, y, z);
                    BlockState scanState = level.getBlockState(scan);
                    if (scanState.getBlock() instanceof GirderStrutBlock) {
                        BlockEntity otherBe = level.getBlockEntity(scan);
                        if (otherBe instanceof GirderStrutBlockEntity other) {
                            // avoid duplicate connection creation by only connecting when this pos is 'smaller' along direction
                            if (!other.hasConnectionTo(pos)) {
                                self.addConnection(scan);
                                other.addConnection(pos);
                                self.setChanged();
                                other.setChanged();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof GirderStrutBlockEntity self) {
                    for (BlockPos otherPos : self.getConnectionsCopy()) {
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
