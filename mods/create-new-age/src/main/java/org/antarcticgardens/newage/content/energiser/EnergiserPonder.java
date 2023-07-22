package org.antarcticgardens.newage.content.energiser;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;

public class EnergiserPonder {

    public static void ponder(SceneBuilder scene, SceneBuildingUtil util) {
        scene.configureBasePlate(0, 0, 5);
        scene.title("energiser", "Using an energiser.");
        scene.addKeyframe();
        scene.idle(40);
        scene.addKeyframe();
        scene.idleSeconds(2);
    }

}