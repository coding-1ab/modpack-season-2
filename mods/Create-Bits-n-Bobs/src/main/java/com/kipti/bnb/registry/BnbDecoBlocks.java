package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;

public class BnbDecoBlocks {

    static {
        CreateBitsnBobs.REGISTRATE.setCreativeTab(BnbCreativeTabs.DECO_CREATIVE_TAB);
    }

//    public static final BlockEntry<ColoredFallingBlock> CLINKER = REGISTRATE.block("clinker", (p) -> new ColoredFallingBlock(new ColorRGBA(0xd2d5d6), p))
//            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
//                    .sound(SoundType.GRAVEL))
//            .transform(b -> b.tag(BlockTags.MINEABLE_WITH_SHOVEL))
//            .simpleItem()
//            .register();

    static {
        CreateBitsnBobs.REGISTRATE.setCreativeTab(BnbCreativeTabs.BASE_CREATIVE_TAB);
    }

    public static void register() {
    }

}
