package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.Map;

public class BnbPartialModels {


//    public static final PartialModel CHAIR_LEFT_ARM = block("chair/chair_left_armrest");
//    public static final PartialModel CHAIR_RIGHT_ARM = block("chair/chair_right_armrest");

    public static final PartialModel
            HEADLAMP_OFF = block("headlamp/headlight"),
            HEADLAMP_ON = block("headlamp/headlight_on"),

    SMALL_COGWHEEL_CHAIN_BLOCK = block("small_cogwheel_chain"),
            LARGE_COGWHEEL_CHAIN_BLOCK = block("large_cogwheel_chain"),


    CHAIN_ROPE_COIL = block("chain_pulley/chain_coil"), CHAIN_ROPE_HALF = block("chain_pulley/chain_rope_half"),
            CHAIN_ROPE_HALF_MAGNET = block("chain_pulley/chain_rope_half_magnet"),
            CHAIN_ROPE = block("chain_pulley/chain_rope"),
            CHAIN_PULLEY_MAGNET_NO_CHAIN = block("chain_pulley/chain_pulley_magnet_no_chain"),
            CHAIN_PULLEY_MAGNET_CHAIN = block("chain_pulley/chain_pulley_magnet_chain"),
            CHAIN_PULLEY_MAGNET_CHAIN_HALF = block("chain_pulley/chain_pulley_magnet_chain_half"),

    ROPE_PULLEY_JEI = createBlock("rope_pulley/item"),

    FLYWHEEL_BEARING_TOP = block("flywheel_bearing/head"),
            LARGE_STONE_COG_SHAFTLESS = block("large_stone_cog_shaftless");


    public static final Map<Direction, PartialModel> WEATHERED_METAL_GIRDER_BRACKETS = new EnumMap<>(Direction.class);

    static {
        for (Direction d : Iterate.horizontalDirections) {
            WEATHERED_METAL_GIRDER_BRACKETS.put(d, block("weathered_metal_girder/bracket_" + Lang.asId(d.name())));
        }
    }

    public static final PartialModel GIRDER_STRUT_SEGMENT = block("girder_strut/girder");
    public static final PartialModel WEATHERED_GIRDER_STRUT_SEGMENT = block("girder_strut/weathered_girder");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateBitsnBobs.asResource("block/" + path));
    }

    private static PartialModel createBlock(String path) {
        return PartialModel.of(Create.asResource("block/" + path));
    }

    public static void register() {
    }
}
