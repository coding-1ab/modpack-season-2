package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.cogwheel_chain.graph.PlacingCogwheelChain;

public class BnbLangEntries {

    public static void register() {
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".base", CreateBitsnBobs.TAB_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".deco", CreateBitsnBobs.DECO_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("message.bits_n_bobs.girder_strut.missing_anchors", "You need %s more Girder Struts");

        PlacingCogwheelChain.ChainAdditionAbortedException.addTranslationLangs(CreateBitsnBobs.REGISTRATE,
                "cannot_revisit_node", "You cannot self-intersect the chain!",
                "out_of_bounds", "Cogwheel exceeds maximum bounds!",
                "cogwheels_cannot_touch", "Cogwheels must not touch each other!",
                "not_flat_connection", "Connections of the same direction must be flat!",
                "no_cogwheel_connection", "Connections with cogwheels must be at right angles!",
                "no_path_to_cogwheel", "No valid path to cogwheel!"
        );

        final String[] entries = {
                //Tooltips
                "block.bits_n_bobs.headlamp.tooltip.summary", "Can be dyed and placed _multiple times in same block_. Useful for trains or fancy signage too!",

                "block.bits_n_bobs.girder_strut.tooltip.summary", "A type of girder used to span a distance _between two anchor points_.",
                "block.bits_n_bobs.weathered_girder_strut.tooltip.summary", "A type of girder used to span a distance _between two anchor points_.",
        };

        for (int i = 0; i < entries.length; i += 2) {
            CreateBitsnBobs.REGISTRATE.addRawLang(entries[i], entries[i + 1]);
        }
    }

}
