package org.antarcticgardens.newage.content.generation;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import net.minecraft.core.Direction;

public class GenerationPonder {


    public static void ponder(SceneBuilder scene, SceneBuildingUtil util) {
        scene.configureBasePlate(0, 0, 9);
        scene.title("generation", "Electricity Generation");
        scene.scaleSceneView(0.7f);
        scene.addKeyframe();

        scene.world.showSection(util.select.layer(0), Direction.UP);

        scene.idle(10);

        scene.world.showSection(util.select.position(5, 1, 7), Direction.DOWN);
        scene.world.showSection(util.select.position(5, 2, 7), Direction.DOWN);

        scene.idle(5);

        scene.world.showSection(util.select.position(3, 1, 7), Direction.DOWN);
        scene.world.showSection(util.select.position(3, 2, 7), Direction.DOWN);

        scene.idle(5);

        scene.world.showSection(util.select.position(3, 1, 3), Direction.DOWN);
        scene.world.showSection(util.select.position(3, 2, 3), Direction.DOWN);

        scene.idle(5);

        scene.world.showSection(util.select.position(5, 1, 3), Direction.DOWN);
        scene.world.showSection(util.select.position(5, 2, 3), Direction.DOWN);

        scene.idle(10);

        scene.world.showSection(util.select.position(5, 3, 7), Direction.DOWN);
        scene.world.showSection(util.select.position(4, 3, 7), Direction.DOWN);
        scene.world.showSection(util.select.position(3, 3, 7), Direction.DOWN);

        scene.idle(10);

        scene.world.showSection(util.select.position(5, 3, 3), Direction.DOWN);
        scene.world.showSection(util.select.position(4, 3, 3), Direction.DOWN);
        scene.world.showSection(util.select.position(3, 3, 3), Direction.DOWN);

        scene.idle(10);

        scene.world.showSection(util.select.fromTo(4, 3, 6, 4, 3, 4), Direction.DOWN);

        scene.idle(15);

        scene.world.showSection(util.select.fromTo(5, 1, 6, 3, 1, 5), Direction.EAST);

        scene.idle(15);

        scene.world.showSection(util.select.fromTo(2, 2, 5, 2, 4, 6), Direction.EAST);

        scene.world.showSection(util.select.fromTo(6, 2, 5, 6, 4, 6), Direction.WEST);

        scene.idle(15);

        scene.world.showSection(util.select.fromTo(5, 5, 6, 3, 5, 5), Direction.DOWN);

    }

}
