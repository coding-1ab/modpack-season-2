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

            BlockModelBuilder chairCornerBack = p.models().withExistingParent(
                    "block/chair/corner_back_" + colourName,
                    p.modLoc("block/chair/corner_back"))
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

                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairCornerBack)
                    .rotationY((int) (direction.toYRot() + 180 + 90) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.LEFT_ARM, false)
                    .condition(ChairBlock.RIGHT_ARM, true)
                    .condition(ChairBlock.CORNER, true)
                    .end();

                p.getMultipartBuilder(c.get())
                    .part()
                    .modelFile(chairCornerBack)
                    .rotationY((int) (direction.toYRot() + 180 - 90) % 360)
                    .addModel()
                    .condition(ChairBlock.FACING, direction)
                    .condition(ChairBlock.RIGHT_ARM, false)
                    .condition(ChairBlock.LEFT_ARM, true)
                    .condition(ChairBlock.CORNER, true)
                    .end();
            }
        };
    }

}
