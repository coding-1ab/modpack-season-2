package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;

public class BnbLangEntries {

    public static void register() {
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".base", CreateBitsnBobs.NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".deco", CreateBitsnBobs.DECO_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("message.bits_n_bobs.girder_strut.missing_anchors", "You need %s more Girder Struts");
    }

}
