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

    public static final BlockEntry<ColoredFallingBlock> CALCINED_FLINT_GRAVEL = REGISTRATE.block("calcined_gravel", (p) -> new ColoredFallingBlock(new ColorRGBA(14406560), p))
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.SAND))
            .transform(b -> b.tag(BlockTags.MINEABLE_WITH_SHOVEL))
            .simpleItem()
            .register();

    public static final BlockEntry<ColoredFallingBlock> DEEP_GRAVEL = REGISTRATE.block("deep_gravel", (p) -> new ColoredFallingBlock(new ColorRGBA(12121260), p))
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK)
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
