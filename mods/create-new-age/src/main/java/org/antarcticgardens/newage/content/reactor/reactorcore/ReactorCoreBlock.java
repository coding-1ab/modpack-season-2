package org.antarcticgardens.newage.content.reactor.reactorcore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.antarcticgardens.newage.NewAgeBlockEntityTypes;
import org.antarcticgardens.newage.content.reactor.ReactorBlock;
import org.jetbrains.annotations.Nullable;

public class ReactorCoreBlock extends ReactorBlock implements EntityBlock {
    public ReactorCoreBlock(Properties properties) {
        super(properties);
    }

    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;


    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, BlockStateProperties.FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return NewAgeBlockEntityTypes.REACTOR_CORE.create(pos, state);
    }
}