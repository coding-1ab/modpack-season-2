package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;

public class BnbDecoBlocks {

    static {
        CreateBitsnBobs.REGISTRATE.setCreativeTab(BnbCreativeTabs.DECO_CREATIVE_TAB);
    }

    public static final BlockEntry<ColoredFallingBlock> CLINKER = REGISTRATE.block("clinker", (p) -> new ColoredFallingBlock(new ColorRGBA(0xd2d5d6), p))
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.SAND))
            .transform(b -> b.tag(BlockTags.MINEABLE_WITH_SHOVEL))
            .simpleItem()
            .register();

    static {
        CreateBitsnBobs.REGISTRATE.setCreativeTab(BnbCreativeTabs.BASE_CREATIVE_TAB);
    }

    public static void register() {
    }

}
