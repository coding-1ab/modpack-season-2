package org.antarcticgardens.cna.content.electricity.light;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LightPoleBlock extends Block {

    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public LightPoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TOP,  true).setValue(BOTTOM,  true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(TOP).add(BOTTOM));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(6, 0, 6, 10, 16, 10);
    }

    public static BlockState updatedState(BlockState state, LevelAccessor world, BlockPos pos) {
        state = state.setValue(TOP, !(world.getBlockState(pos.relative(Direction.UP)).getBlock() instanceof LightPoleBlock));
        state = state.setValue(BOTTOM, !(world.getBlockState(pos.relative(Direction.DOWN)).getBlock() instanceof LightPoleBlock));
        return state;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return updatedState(state, world, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        level.setBlockAndUpdate(pos, updatedState(state, level, pos));
    }
}
