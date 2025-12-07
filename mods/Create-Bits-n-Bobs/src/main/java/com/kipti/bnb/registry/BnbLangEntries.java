package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.cogwheel_chain.graph.ChainInteractionFailedException;

public class BnbLangEntries {

    public static void register() {
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".base", CreateBitsnBobs.TAB_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".deco", CreateBitsnBobs.DECO_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("message.bits_n_bobs.girder_strut.missing_anchors", "You need %s more Girder Struts");

        ChainInteractionFailedException.addTranslationLangs(CreateBitsnBobs.REGISTRATE,
                "cannot_revisit_node", "You cannot self-intersect the chain!",
                "out_of_bounds", "Cogwheel exceeds maximum bounds!",
                "cogwheels_cannot_touch", "Cogwheels must not touch each other!",
                "not_flat_connection", "Connections of the same direction must be flat!",
                "no_cogwheel_connection", "Connections with cogwheels must be at right angles!",
                "no_path_to_cogwheel", "No valid path to cogwheel!",
                "config_forbids", "Server has disabled chain drives!",

                //Pathfinding errors, should not happen, since the validation in chain placement, but in case
                "pathfinding_failed_at_node", "Couldn't find valid path between two nodes! (Try inserting more nodes?)",
                "pathfinding_failed", "Couldn't find valid path across chain! (Try inserting more nodes?)"
        );

        final String[] entries = {
                //Tooltips
                "block.bits_n_bobs.headlamp.tooltip.summary", "Can be dyed and placed _multiple times in same block_. Useful for trains or fancy signage too!",

                "block.bits_n_bobs.lightbulb.tooltip.summary", "_Its a lightbulb_, what do you expect.",
                "block.bits_n_bobs.lightbulb.tooltip.condition1", "When R-Clicked with Wrench",
                "block.bits_n_bobs.lightbulb.tooltip.behaviour1", "Toggles the lightbulb _cage variant_.",

                "block.bits_n_bobs.girder_strut.tooltip.summary", "A type of girder used to span a distance _between two anchor points_.",
                "block.bits_n_bobs.weathered_girder_strut.tooltip.summary", "A type of girder used to span a distance _between two anchor points_.",

                "block.bits_n_bobs.chair.tooltip.summary", "Sit yourself down and enjoy the ride! Will anchor a player onto a moving _contraption_. Even _fancier than a seat_ for static furniture too! Comes in a variety of colours.",
                "block.bits_n_bobs.chair.tooltip.condition1", "Right click on Chair",
                "block.bits_n_bobs.chair.tooltip.behaviour1", "Sits the player on the _Chair_. Press L-shift to leave the _Chair_.",
        };

        for (int i = 0; i < entries.length; i += 2) {
            CreateBitsnBobs.REGISTRATE.addRawLang(entries[i], entries[i + 1]);
        }
    }

}
