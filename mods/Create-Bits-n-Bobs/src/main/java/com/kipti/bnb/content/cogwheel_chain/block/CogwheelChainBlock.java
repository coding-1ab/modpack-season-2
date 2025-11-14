package com.kipti.bnb.content.cogwheel_chain.block;

import com.kipti.bnb.registry.BnbBlockEntities;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CogwheelChainBlock extends RotatedPillarKineticBlock
    implements IBE<CogwheelChainBlockEntity> { //TODO : waterlog state

    protected CogwheelChainBlock(boolean large, Properties properties) {
        super(properties);
        isLarge = large;
    }

    public boolean isLargeChainCog() {
        return isLarge;
    }

    boolean isLarge;

    public static CogwheelChainBlock small(Properties properties) {
        return new CogwheelChainBlock(false, properties);
    }

    public static CogwheelChainBlock large(Properties properties) {
        return new CogwheelChainBlock(true, properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return (isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR).get(state.getValue(AXIS));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.getValue(AXIS));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, world, pos, newState);
    }

    @Override
    public Class<CogwheelChainBlockEntity> getBlockEntityClass() {
        return CogwheelChainBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CogwheelChainBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.COGWHEEL_CHAIN.get();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    public float getRadius() {
        return isLarge ? 1f : 0.5f;
    }
}
