package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.light.headlamp.HeadlampBlockEntity;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import java.util.Arrays;

public class BnbPartialModels {

    public static final PartialModel HEADLAMP_OFF = block("headlamp/headlight");
    public static final PartialModel HEADLAMP_ON = block("headlamp/headlight_on");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateBitsnBobs.asResource("block/" + path));
    }

    public static void register() {
    }
}
