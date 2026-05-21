package dev.propulsionteam.propulsionsimulated.content.tilt_adapter;

import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;

public class TiltAdapterBlock extends AbstractTiltAdapterBlock<TiltAdapterBlockEntity> {

    public TiltAdapterBlock(Properties properties) {
        super(properties, TiltAdapterBlockEntity.class,
            PropulsionBlockEntities.TILT_ADAPTER_BLOCK_ENTITY::get,
            TiltAdapterBlockEntity::new);
    }
}
