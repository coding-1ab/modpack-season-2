package com.kipti.bnb.registry;

import com.kipti.bnb.content.palette.BnbPaletteBlockPattern;
import com.kipti.bnb.content.palette.BnbPalettesVariantEntry;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

import static com.kipti.bnb.content.palette.BnbPaletteBlockPattern.ADDITIONS_TO_BASE;

public enum BnbPaletteStoneTypes {

    GRANITE(ADDITIONS_TO_BASE, r -> () -> Blocks.GRANITE),
    DIORITE(ADDITIONS_TO_BASE, r -> () -> Blocks.DIORITE),
    ANDESITE(ADDITIONS_TO_BASE, r -> () -> Blocks.ANDESITE),
    CALCITE(ADDITIONS_TO_BASE, r -> () -> Blocks.CALCITE),
    DRIPSTONE(ADDITIONS_TO_BASE, r -> () -> Blocks.DRIPSTONE_BLOCK),
    DEEPSLATE(ADDITIONS_TO_BASE, r -> () -> Blocks.DEEPSLATE),
    TUFF(ADDITIONS_TO_BASE, r -> () -> Blocks.TUFF),

    ASURINE(ADDITIONS_TO_BASE, r -> () -> AllPaletteStoneTypes.ASURINE.baseBlock.get(), (p) -> p.destroyTime(1.25f).mapColor(MapColor.COLOR_BLUE)),
    CRIMSITE(ADDITIONS_TO_BASE, r -> () -> AllPaletteStoneTypes.CRIMSITE.baseBlock.get(), (p) -> p.destroyTime(1.25f).mapColor(MapColor.COLOR_RED)),
    LIMESTONE(ADDITIONS_TO_BASE, r -> () -> AllPaletteStoneTypes.LIMESTONE.baseBlock.get(), (p) -> p.destroyTime(1.25f).mapColor(MapColor.SAND)),
    OCHRUM(ADDITIONS_TO_BASE, r -> () -> AllPaletteStoneTypes.OCHRUM.baseBlock.get(), (p) -> p.destroyTime(1.25f).mapColor(MapColor.TERRACOTTA_YELLOW)),
    SCORIA(ADDITIONS_TO_BASE, r -> () -> AllPaletteStoneTypes.SCORIA.baseBlock.get(), (p) -> p.mapColor(MapColor.COLOR_BROWN)),
    SCORCHIA(ADDITIONS_TO_BASE, r -> () -> AllPaletteStoneTypes.SCORCHIA.baseBlock.get(), (p) -> p.mapColor(MapColor.TERRACOTTA_GRAY)),
    VERIDIUM(ADDITIONS_TO_BASE, r -> () -> AllPaletteStoneTypes.VERIDIUM.baseBlock.get(), (p) -> p.destroyTime(1.25f).mapColor(MapColor.WARPED_NYLIUM)),

    ;

    private Function<CreateRegistrate, NonNullSupplier<Block>> factory;
    private BnbPalettesVariantEntry variants;

    public NonNullSupplier<Block> baseBlock;
    public NonNullFunction<BlockBuilder<? extends Block, CreateRegistrate>, BlockBuilder<? extends Block, CreateRegistrate>> modifyProperties;
    public BnbPaletteBlockPattern[] variantTypes;
    public TagKey<Item> materialTag;

    BnbPaletteStoneTypes(BnbPaletteBlockPattern[] variantTypes,
                         Function<CreateRegistrate, NonNullSupplier<Block>> baseBlockSupplier) {
        this.factory = baseBlockSupplier;
        this.variantTypes = variantTypes;
        modifyProperties = b -> b.initialProperties(baseBlock);
    }

    BnbPaletteStoneTypes(BnbPaletteBlockPattern[] variantTypes,
                         Function<CreateRegistrate, NonNullSupplier<Block>> baseBlockSupplier,
                         NonNullUnaryOperator<BlockBehaviour.Properties> modifyProperties) {
        this.factory = baseBlockSupplier;
        this.variantTypes = variantTypes;
        this.modifyProperties = b -> b.properties(modifyProperties);
    }


    public NonNullSupplier<Block> getBaseBlock() {
        return baseBlock;
    }

    public BnbPalettesVariantEntry getVariants() {
        return variants;
    }

    public static void register(CreateRegistrate registrate) {
        for (BnbPaletteStoneTypes paletteStoneVariants : values()) {
            NonNullSupplier<Block> baseBlock = paletteStoneVariants.factory.apply(registrate);
            paletteStoneVariants.baseBlock = baseBlock;
            String id = Lang.asId(paletteStoneVariants.name());
            paletteStoneVariants.materialTag =
                    AllTags.optionalTag(BuiltInRegistries.ITEM, Create.asResource("stone_types/" + id));
            paletteStoneVariants.variants = new BnbPalettesVariantEntry(id, paletteStoneVariants);
        }
    }

}
