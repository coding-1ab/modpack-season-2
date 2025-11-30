package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;

public class BnbLangEntries {

    public static void register() {
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".base", CreateBitsnBobs.TAB_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".deco", CreateBitsnBobs.DECO_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("message.bits_n_bobs.girder_strut.missing_anchors", "You need %s more Girder Struts");

        final String[] entries = {
                "block.bits_n_bobs.headlamp.tooltip.summary", "Can be dyed and placed _multiple times in same block_. Useful for trains or fancy signage too!",
        };

        for (int i = 0; i < entries.length; i += 2) {
            CreateBitsnBobs.REGISTRATE.addRawLang(entries[i], entries[i + 1]);
        }
    }

}
