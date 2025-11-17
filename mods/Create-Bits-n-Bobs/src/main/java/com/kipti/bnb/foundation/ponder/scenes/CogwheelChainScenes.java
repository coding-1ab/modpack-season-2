package com.kipti.bnb.foundation.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

public class CogwheelChainScenes {

    public static void flatCogwheelChain(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chain_cog_flat", "Connecting cogwheels with a chain");
        scene.configureBasePlate(0, 0, 7);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.world().setBlock(new BlockPos(1, 1, 1), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(2, 1, 2), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(4, 1, 5), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(4, 1, 2), AllBlocks.LARGE_COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(2, 1, 4), AllBlocks.LARGE_COGWHEEL.getDefaultState(), false);

        scene.world().setKineticSpeed(util.select().position(4, 0, 2), 16f);
        scene.world().setKineticSpeed(util.select().position(4, 1, 2), 16f);

        scene.idle(20);

        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);

        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(4, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 1, 4), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();

        scene.idle(20);

        scene.overlay().showText(40)
                .text("A chain can be used to connect 2 or more cogwheels")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST));

        scene.idle(60);
        scene.overlay().showControls(util.vector().centerOf(4, 1, 2), Pointing.DOWN, 60)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 2), Pointing.DOWN, 50)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(1, 1, 1), Pointing.DOWN, 40)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 4), Pointing.DOWN, 30)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(4, 1, 5), Pointing.DOWN, 20)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.addKeyframe();
        scene.idle(15);

        scene.world().restoreBlocks(util.select().layer(1));

        scene.world().setKineticSpeed(util.select().position(4, 1, 2), 16f);
        scene.world().setKineticSpeed(util.select().position(2, 1, 2), -32f);
        scene.world().setKineticSpeed(util.select().position(1, 1, 1), 32f);
        scene.world().setKineticSpeed(util.select().position(2, 1, 4), 16f);
        scene.world().setKineticSpeed(util.select().position(4, 1, 5), 32f);

        scene.idle(40);
        scene.markAsFinished();
    }


}
