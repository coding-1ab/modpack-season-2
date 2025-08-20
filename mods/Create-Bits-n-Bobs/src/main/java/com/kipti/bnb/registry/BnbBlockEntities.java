package com.kipti.bnb.registry;

import com.kipti.bnb.content.girder_strut.GirderStrutBlockEntity;
import com.kipti.bnb.content.girder_strut.GirderStrutBlockEntityRenderer;
import com.kipti.bnb.content.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.content.nixie.foundation.GenericNixieDisplayBoardRenderer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;

public class BnbBlockEntities {

    public static final BlockEntityEntry<HeadlampBlockEntity> HEADLAMP = REGISTRATE.blockEntity("headlamp", HeadlampBlockEntity::new)
        .validBlock(BnbBlocks.HEADLAMP)
        .register();

    public static final BlockEntityEntry<KineticBlockEntity> ENCASED_SHAFT = REGISTRATE
        .blockEntity("encased_shaft", KineticBlockEntity::new)
        .visual(() -> SingleAxisRotatingVisual::shaft, false)
        .validBlocks(BnbBlocks.WEATHERED_METAL_GIRDER_ENCASED_SHAFT)
        .renderer(() -> ShaftRenderer::new)
        .register();

    public static final BlockEntityEntry<GenericNixieDisplayBlockEntity> GENERIC_NIXIE_DISPLAY = REGISTRATE.blockEntity("generic_nixie_display", GenericNixieDisplayBlockEntity::new)
        .validBlocks(BnbBlocks.NIXIE_BOARD, BnbBlocks.LARGE_NIXIE_TUBE)
        .validBlocks(BnbBlocks.DYED_NIXIE_BOARD.toArray())
        .validBlocks(BnbBlocks.DYED_LARGE_NIXIE_TUBE.toArray())
        .renderer(() -> GenericNixieDisplayBoardRenderer::new)
        .register();

    public static final BlockEntityEntry<GirderStrutBlockEntity> GIRDER_STRUT = REGISTRATE.blockEntity("girder_strut", GirderStrutBlockEntity::new)
        .validBlock(BnbBlocks.GIRDER_STRUT)
        .renderer(() -> GirderStrutBlockEntityRenderer::new)
        .register();

    public static void register() {
    }

}
