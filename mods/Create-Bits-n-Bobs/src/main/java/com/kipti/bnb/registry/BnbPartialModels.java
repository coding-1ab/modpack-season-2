package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class BnbPartialModels {

    public static final PartialModel HEADLAMP_OFF = block("headlamp/headlight");
    public static final PartialModel HEADLAMP_ON = block("headlamp/headlight_on");

    public static final PartialModel CHAIR_LEFT_ARM = block("chair/chair_left_armrest");
    public static final PartialModel CHAIR_RIGHT_ARM = block("chair/chair_right_armrest");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateBitsnBobs.asResource("block/" + path));
    }

    public static void register() {
    }
}
