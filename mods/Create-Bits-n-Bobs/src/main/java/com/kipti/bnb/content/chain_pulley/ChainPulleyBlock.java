package com.kipti.bnb.content.chain_pulley;

import com.kipti.bnb.registry.BnbBlockEntities;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ChainPulleyBlock extends PulleyBlock {
    public ChainPulleyBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends PulleyBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.CHAIN_ROPE_PULLEY.get();
    }
}
