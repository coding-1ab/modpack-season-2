package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.Map;

public class BnbPartialModels {

    public static final PartialModel HEADLAMP_OFF = block("headlamp/headlight");
    public static final PartialModel HEADLAMP_ON = block("headlamp/headlight_on");

    public static final PartialModel CHAIR_LEFT_ARM = block("chair/chair_left_armrest");
    public static final PartialModel CHAIR_RIGHT_ARM = block("chair/chair_right_armrest");

    public static final Map<Direction, PartialModel> WEATHERED_METAL_GIRDER_BRACKETS = new EnumMap<>(Direction.class);

    static {
        for (Direction d : Iterate.horizontalDirections) {
            WEATHERED_METAL_GIRDER_BRACKETS.put(d, block("weathered_metal_girder/bracket_" + Lang.asId(d.name())));
        }
    }

    private static PartialModel block(String path) {
        return PartialModel.of(CreateBitsnBobs.asResource("block/" + path));
    }

    public static void register() {
    }
}
