package dev.propulsionteam.propulsionsimulated.content.thruster.liquid_vector_thruster;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import com.mojang.serialization.MapCodec;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterBlock;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
    @SuppressWarnings("unchecked")
    public Class<AbstractThrusterBlockEntity> getBlockEntityClass() {
        return (Class<AbstractThrusterBlockEntity>) (Object) LiquidVectorThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LiquidVectorThrusterBlockEntity> getBlockEntityType() {
        return PropulsionBlockEntities.LIQUID_VECTOR_THRUSTER_BLOCK_ENTITY.get();
    }
}