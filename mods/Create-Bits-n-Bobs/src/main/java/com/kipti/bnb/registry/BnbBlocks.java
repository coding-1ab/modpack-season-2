package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.chair.ChairBlock;
import com.kipti.bnb.content.chair.ChairModelBuilder;
import com.kipti.bnb.content.light.founation.LightBlock;
import com.kipti.bnb.content.light.headlamp.HeadlampBlock;
import com.kipti.bnb.content.light.headlamp.HeadlampBlockItem;
import com.kipti.bnb.content.light.headlamp.HeadlampModelBuilder;
import com.kipti.bnb.content.light.lightbulb.LightbulbBlock;
import com.simibubi.create.AllDisplaySources;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.TagGen.axeOnly;
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


    public static final DyedBlockList<ChairBlock> CHAIRS = new DyedBlockList<>(colour -> {
        String colourName = colour.getSerializedName();
        SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
        SeatInteractionBehaviour interactionBehaviour = new SeatInteractionBehaviour();
        return REGISTRATE.block(colourName + "_chair", p -> new ChairBlock(p, colour))
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.mapColor(colour))
            .properties(p -> p.noOcclusion())
            .transform(axeOnly())
            .onRegister(movementBehaviour(movementBehaviour))
            .onRegister(interactionBehaviour(interactionBehaviour))
            .transform(displaySource(AllDisplaySources.ENTITY_NAME))
//            .onRegister(CreateRegistrate.blockModel(() -> ChairModelBuilder::new))
            .blockstate((c, p) -> {
                BlockModelBuilder chairBlock = p.models().withExistingParent("block/chair/block_" + colourName, p.modLoc("block/chair/block"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName));
                BlockModelBuilder chairLeftArmrest = p.models().withExistingParent(
                        "block/chair/left_armrest_" + colourName,
                        p.modLoc("block/chair/left_armrest"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName));
                BlockModelBuilder chairRightArmrest = p.models().withExistingParent(
                        "block/chair/right_armrest_" + colourName,
                        p.modLoc("block/chair/right_armrest"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName));
                for (Direction direction : Iterate.horizontalDirections) {
                    p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairBlock)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .end();
                    p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairLeftArmrest)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.LEFT_ARM, true)
                        .end();
                    p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairRightArmrest)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.RIGHT_ARM, true)
                        .end();
                }
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .recipe((c, p) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
                    .requires(DyeHelper.getWoolOfDye(colour))
                    .requires(ItemTags.WOODEN_SLABS)
                    .unlockedBy("has_wool", RegistrateRecipeProvider.has(ItemTags.WOOL))
                    .save(p, CreateBitsnBobs.asResource("crafting/kinetics/" + c.getName()));
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
                    .requires(colour.getTag())
                    .requires(AllTags.AllItemTags.SEATS.tag)
                    .unlockedBy("has_seat", RegistrateRecipeProvider.has(AllTags.AllItemTags.SEATS.tag))
                    .save(p, CreateBitsnBobs.asResource("crafting/kinetics/" + c.getName() + "_from_other_chair"));
            })
//            .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.create.seat"))
            .tag(BnbTags.BnbBlockTags.CHAIRS.tag)
            .item()
            .model((c, p) ->
                p.withExistingParent("item/" + colourName + "_chair", p.modLoc("block/chair/item"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName)))
            .tag(BnbTags.BnbItemTags.CHAIRS.tag)
            .build()
            .register();
    });

    public static void register() {
    }

}
