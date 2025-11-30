package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.minecraft.resources.ResourceLocation;

public class BnbSpriteShifts {

    public static final SpriteShiftEntry
            CHAIN_PULLEY_COIL = get("block/chain_pulley_coil", "block/chain_pulley_coil_scroll"),
            CHAIN_ROPE = SpriteShifter.get(ResourceLocation.withDefaultNamespace("block/chain"), CreateBitsnBobs.asResource("block/chain_scroll"));

    private static SpriteShiftEntry get(final String originalLocation, final String targetLocation) {
        return SpriteShifter.get(CreateBitsnBobs.asResource(originalLocation), CreateBitsnBobs.asResource(targetLocation));
    }

    public static void register() {
    }

}
