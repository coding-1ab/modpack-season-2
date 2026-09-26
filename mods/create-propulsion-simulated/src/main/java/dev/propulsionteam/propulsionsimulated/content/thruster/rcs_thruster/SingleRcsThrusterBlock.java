package dev.propulsionteam.propulsionsimulated.content.thruster.rcs_thruster;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.DirectionalBlock;

public class SingleRcsThrusterBlock extends RcsThrusterBlock {
    public static final MapCodec<SingleRcsThrusterBlock> CODEC = simpleCodec(SingleRcsThrusterBlock::new);

    public SingleRcsThrusterBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }

    @Override
    public boolean isSingle() { return true; }
}
