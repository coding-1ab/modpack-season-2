package com.kipti.bnb.content.chair;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import org.jetbrains.annotations.NotNull;

public class ChairBlockStateGen {

    public static @NotNull NonNullBiConsumer<DataGenContext<Block, ChairBlock>, RegistrateBlockstateProvider> dyedChair(String colourName) {
        return (c, p) -> {
            BlockModelBuilder chairBaseBlock = p.models().withExistingParent("block/chair/block_single_" + colourName, p.modLoc("block/chair/block_single"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);
            BlockModelBuilder chairCornerBaseBlock = p.models().withExistingParent("block/chair/block_corner_" + colourName, p.modLoc("block/chair/block_corner"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);

            BlockModelBuilder chairLeftArmrest = p.models().withExistingParent(
                    "block/chair/left_armrest_" + colourName,
                    p.modLoc("block/chair/left_armrest"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);

            BlockModelBuilder chairRightArmrest = p.models().withExistingParent(
                    "block/chair/right_armrest_" + colourName,
                    p.modLoc("block/chair/right_armrest"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);

            BlockModelBuilder chairBack = p.models().withExistingParent(
                    "block/chair/chair_back_" + colourName,
                    p.modLoc("block/chair/chair_back"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);
            BlockModelBuilder chairBackFlat = p.models().withExistingParent(
                    "block/chair/chair_back_flat_" + colourName,
                    p.modLoc("block/chair/chair_back_flat"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);

            BlockModelBuilder chairBackFlatTrimmedRight = p.models().withExistingParent(
                    "block/chair/chair_back_flat_trimmed_right_" + colourName,
                    p.modLoc("block/chair/chair_back_flat_trimmed_right"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);
            BlockModelBuilder chairBackFlatTrimmedLeft = p.models().withExistingParent(
                    "block/chair/chair_back_flat_trimmed_left_" + colourName,
                    p.modLoc("block/chair/chair_back_flat_trimmed_left"))
                .texture("2", p.modLoc("block/chair/chair_" + colourName))
                .ao(false);

            for (Direction direction : Iterate.horizontalDirections) {
                //Base seat model
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBaseBlock)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.CORNER, false)
                    .end();
                //- 'Confusion' fallback, keep the normal model since there will be no arms
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBaseBlock)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .end();
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBaseBlock)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .end();
                //- Corner base model
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairCornerBaseBlock)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .end();
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairCornerBaseBlock)
                    .rotationY((int) (direction.toYRot() + 180 - 90) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .end();

                //Armrests
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairLeftArmrest)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.CORNER, false)
                    .end();
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairRightArmrest)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .condition(ChairBlock.CORNER, false)
                    .end();

                //Corner back
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBack)
                    .rotationY((int) (direction.toYRot() + 180 + 90) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.BACK_FLAT, false)
                    .end();

                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBack)
                    .rotationY((int) (direction.toYRot() + 180 - 90) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.BACK_FLAT, false)
                    .end();

                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBackFlatTrimmedLeft)
                    .rotationY((int) (direction.toYRot() + 180 + 90) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .end();
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBackFlatTrimmedRight)
                    .rotationY((int) (direction.toYRot() + 180 - 90) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .end();

                //Flat component of corner back
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBack)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.BACK_FLAT, false)
                    .end();

                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBack)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.BACK_FLAT, false)
                    .end();

                //Base back

                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBack)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, false)
                    .condition(ChairBlock.CORNER, false)
                    .end();
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBackFlat)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, true)
                    .condition(ChairBlock.CORNER, false)
                    .end();

                //Confusion states again
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBackFlat)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .end();
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBackFlat)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .end();

                //Corner trimmed states (avoiding z clipping)
                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBackFlatTrimmedRight)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .end();

                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairBackFlatTrimmedLeft)
                    .rotationY((int) (direction.toYRot() + 180) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.BACK_FLAT, true)
                    .condition(ChairBlock.CORNER, true)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .end();
            }
        };
    }

}
