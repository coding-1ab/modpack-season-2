package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.light.founation.LightBlock;
import com.kipti.bnb.content.light.headlamp.HeadlampBlock;
import com.kipti.bnb.content.light.headlamp.HeadlampBlockItem;
import com.kipti.bnb.content.light.headlamp.HeadlampModelBuilder;
import com.kipti.bnb.content.light.lightbulb.LightbulbBlock;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.DyeColor;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BnbBlocks {

    public static final BlockEntry<LightbulbBlock> LIGHTBULB = REGISTRATE.block("lightbulb", LightbulbBlock::new)
        .initialProperties(SharedProperties::softMetal)
        .transform(pickaxeOnly())
        .blockstate((c, p) -> p.directionalBlock(c.get(),
            (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                "block/lightbulb/lightbulb" + (state.getValue(LightbulbBlock.CAGE) ? "" : "_uncaged") + (state.getValue(LightBlock.LIT) ? "_on" : "")
            ))))
        .properties(p -> p
            .noOcclusion()
            .lightLevel(state -> state.getValue(LightBlock.LIT) ? 10 : 0)
            .emissiveRendering((state, level, pos) -> state.getValue(LightBlock.LIT))
            .forceSolidOn())
        .addLayer(() -> RenderType::translucent)
        .item()
        .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/lightbulb/lightbulb")))
        .build()
        .register();

    public static final BlockEntry<LightBlock> BRASS_LAMP = REGISTRATE.block("brass_lamp", (p) -> new LightBlock(p, BnbShapes.BRASS_LAMP_SHAPE))
        .initialProperties(SharedProperties::softMetal)
        .transform(pickaxeOnly())
        .blockstate((c, p) -> p.directionalBlock(c.get(),
            (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                "block/brass_lamp/brass_lamp" + (state.getValue(LightBlock.LIT) ? "_on" : "")
            ))))
        .properties(p -> p
            .noOcclusion()
            .lightLevel(state -> state.getValue(LightBlock.LIT) ? 15 : 0)
            .mapColor(DyeColor.ORANGE)
            .forceSolidOn())
        .addLayer(() -> RenderType::translucent)
        .item()
        .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/brass_lamp/brass_lamp")))
        .build()
        .register();

    public static final BlockEntry<HeadlampBlock> HEADLAMP = REGISTRATE.block("headlamp", HeadlampBlock::new)
        .initialProperties(SharedProperties::softMetal)
        .transform(pickaxeOnly())
        .blockstate((c, p) -> p.simpleBlock(c.get(),
            p.models().getExistingFile(CreateBitsnBobs.asResource(
                "block/headlamp/headlight_block"
            ))
        ))
        .onRegister(CreateRegistrate.blockModel(() -> HeadlampModelBuilder::new))
        .properties(p -> p
            .noOcclusion()
            .lightLevel(state -> state.getValue(LightBlock.LIT) ? 15 : 0)
            .emissiveRendering((state, level, pos) -> state.getValue(LightBlock.LIT))
            .mapColor(DyeColor.ORANGE)
            .forceSolidOn())
        .addLayer(() -> RenderType::translucent)
        .item(HeadlampBlockItem::new)
        .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/headlamp/headlight")))
        .build()
        .register();

    public static void register() {
    }

}
