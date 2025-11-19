package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;

public class BnbLangEntries {

    public static void register() {
        CreateBitsnBobs.REGISTRATE.addRawLang("tab.bnb_based", CreateBitsnBobs.NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("tab.bnb_deco", "Create: Bits 'n' Bobs Decoration");
        CreateBitsnBobs.REGISTRATE.addRawLang("message.bits_n_bobs.girder_strut.missing_anchors", "You need %s more Girder Struts");
    }

}
