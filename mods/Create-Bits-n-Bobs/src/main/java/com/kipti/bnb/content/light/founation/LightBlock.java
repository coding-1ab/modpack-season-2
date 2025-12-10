package com.kipti.bnb.content.light.founation;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightBlock extends DirectionalBlock implements IWrenchable {

    public final MapCodec<LightBlock> CODEC;

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    private final VoxelShaper shaper;

    private final boolean forcePlaceUpwards;

    public LightBlock(final Properties p_52591_, final VoxelShaper shaper) {
        this(p_52591_, shaper, false);
    }

    public LightBlock(final Properties p_52591_, final VoxelShaper shaper, final boolean forcePlaceUpwards) {
        super(p_52591_);
        this.shaper = shaper;
        registerDefaultState(defaultBlockState().setValue(POWER, 0));
        CODEC = simpleCodec((p) -> new LightBlock(p, shaper, forcePlaceUpwards));
        this.forcePlaceUpwards = forcePlaceUpwards;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER, FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull final BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING,
                        forcePlaceUpwards ?
                                ((context.getPlayer() != null && context.getPlayer().isCrouching()) ? context.getClickedFace().getOpposite() : Direction.UP) :
                                context.getClickedFace()
                )
                .setValue(POWER, context.getLevel().getBestNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos pos, @NotNull final CollisionContext context) {
        return shaper.get(state.getValue(FACING));
    }

    @Override
    public boolean canConnectRedstone(@NotNull final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos pos, @Nullable final Direction direction) {
        return true;
    }

    @Override
    protected void neighborChanged(@NotNull final BlockState state, final Level level, @NotNull final BlockPos pos, @NotNull final Block block, @NotNull final BlockPos fromPos, final boolean isMoving) {
        if (!level.isClientSide) {
            final int currentPower = state.getValue(POWER);
            final int signal = level.getBestNeighborSignal(pos);
            if (currentPower != signal) {
                if (currentPower > 0) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    level.setBlock(pos, state.setValue(POWER, signal), 2);
                }
            }
        }
    }

    @Override
    protected void tick(final BlockState state, @NotNull final ServerLevel level, @NotNull final BlockPos pos, @NotNull final RandomSource random) {
        final int signal = level.getBestNeighborSignal(pos);
        if (state.getValue(POWER) != signal) {
            level.setBlock(pos, state.setValue(POWER, signal), 2);
        }
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

}
