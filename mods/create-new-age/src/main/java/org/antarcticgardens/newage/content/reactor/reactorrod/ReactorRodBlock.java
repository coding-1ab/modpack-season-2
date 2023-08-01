package org.antarcticgardens.newage.content.reactor.reactorrod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.antarcticgardens.newage.NewAgeBlockEntityTypes;
import org.antarcticgardens.newage.NewAgeBlocks;
import org.antarcticgardens.newage.content.reactor.ReactorBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReactorRodBlock extends ReactorBlock implements EntityBlock {
    public ReactorRodBlock(Properties properties) {
        super(properties
                .isViewBlocking((blockState, blockGetter, blockPos) -> false)
                .lightLevel(state -> state.getValue(ACTIVE) ? 12 : 0));
    }

    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;

    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ACTIVE, false);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.getValue(ACTIVE)) {
            level.setBlock(pos, NewAgeBlocks.CORIUM.getDefaultState(), 3);
            if (level instanceof ServerLevel lvl) {
                lvl.explode(null, pos.getX(), pos.getY(), pos.getZ(), 2.0f, true, Level.ExplosionInteraction.TNT);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return state.getValue(ACTIVE) ? new ArrayList<>() : super.getDrops(state, params);
    }


    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (state.getValue(ACTIVE))
            return InteractionResult.PASS;
        else {
            return super.onSneakWrenched(state, context);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return NewAgeBlockEntityTypes.REACTOR_ROD.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return (level1, blockPos, blockState, blockEntity) -> {
            if (!(blockEntity instanceof ReactorRodBlockEntity ent)) return;
            ent.tick(blockPos, level1, blockState);
        };
    }

}