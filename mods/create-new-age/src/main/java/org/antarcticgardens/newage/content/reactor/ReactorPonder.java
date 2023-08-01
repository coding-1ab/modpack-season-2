package org.antarcticgardens.newage.content.reactor;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import net.minecraft.core.Direction;

public class ReactorPonder {

    public static void ponder(SceneBuilder scene, SceneBuildingUtil util) {
        scene.configureBasePlate(0, 0, 9);
        scene.title("reactor", "Generating Electricity the Nuclear Way");
        scene.showBasePlate();

        scene.world.showSection(util.select.fromTo(0, 0, 0, 9, 0, 9), Direction.UP);

        scene.idleSeconds(1);

    }

}
