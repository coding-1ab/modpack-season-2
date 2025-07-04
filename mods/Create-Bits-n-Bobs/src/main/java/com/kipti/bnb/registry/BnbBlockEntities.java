package com.kipti.bnb.registry;

import com.kipti.bnb.content.light.headlamp.HeadlampBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;

public class BnbBlockEntities {

    public static final BlockEntityEntry<HeadlampBlockEntity> HEADLAMP = REGISTRATE.blockEntity("headlamp", HeadlampBlockEntity::new)
        .validBlock(BnbBlocks.HEADLAMP)
        .register();

    public static void register() {
    }

}
