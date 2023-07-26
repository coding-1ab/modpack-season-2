package org.antarcticgardens.newage.content.generation.connector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElectricalConnectorBlock extends FaceAttachedHorizontalDirectionalBlock {
    public ElectricalConnectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACE)) {
            case WALL:
                switch (state.getValue(FACING)) {
                    case NORTH:
                        return Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
                    case SOUTH:
                        return Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
                    case WEST:
                        return Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
                    case EAST:
                    default:
                        return Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
                }

            case FLOOR:
                return Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);

            case CEILING:
            default:
                return Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
        }
    }
}
