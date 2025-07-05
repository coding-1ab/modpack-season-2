package com.kipti.bnb.content.nixie.foundation;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class DoubleOrientedBlock extends DirectionalBlock {

    public static final MapCodec<DoubleOrientedBlock> CODEC = simpleCodec(DoubleOrientedBlock::new);

    public static final DirectionProperty ORIENTATION = DirectionProperty.create("orientation");
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public DoubleOrientedBlock(Properties p_52591_) {
        super(p_52591_);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(ORIENTATION, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ORIENTATION, LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction surface = context.getClickedFace();
        Direction facing = context.getNearestLookingDirection();
        if (facing.getAxis() == surface.getAxis()) {
            facing = Arrays.stream(Direction.values())
                .filter(dir -> dir.getAxis() != surface.getAxis())

                .min(Comparator.comparingDouble(dir -> Vec3.atLowerCornerOf(dir.getNormal())
                    .distanceToSqr(Objects.requireNonNull(context.getPlayer()).getLookAngle())))

                .orElse(Direction.NORTH);
        }
        BlockState stateForPlacement = super.getStateForPlacement(context);
        return stateForPlacement == null ? null : stateForPlacement
            .setValue(FACING, surface)
            .setValue(ORIENTATION, facing.getOpposite());
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

}
