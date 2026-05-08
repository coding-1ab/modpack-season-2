package dev.propulsionteam.propulsionsimulated.content.thruster.liquid_vector_thruster;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import com.mojang.serialization.MapCodec;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterShapes;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterBlock;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidVectorThrusterBlock extends VectorThrusterBlock {
    public static final MapCodec<LiquidVectorThrusterBlock> CODEC = simpleCodec(LiquidVectorThrusterBlock::new);

    public LiquidVectorThrusterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return ThrusterShapes.VECTOR_THRUSTER.get(facing);
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<AbstractThrusterBlockEntity> getBlockEntityClass() {
        return (Class<AbstractThrusterBlockEntity>) (Object) LiquidVectorThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LiquidVectorThrusterBlockEntity> getBlockEntityType() {
        return PropulsionBlockEntities.LIQUID_VECTOR_THRUSTER_BLOCK_ENTITY.get();
    }
}