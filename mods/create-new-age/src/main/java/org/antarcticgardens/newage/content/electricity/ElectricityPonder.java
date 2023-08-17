package org.antarcticgardens.newage.content.electricity;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.newage.NewAgeItems;
import org.antarcticgardens.newage.content.electricity.connector.ElectricalConnectorBlockEntity;
import org.antarcticgardens.newage.content.electricity.wire.WireType;

public class ElectricityPonder {

    public static void ponder(SceneBuilder scene, SceneBuildingUtil util) {
        scene.configureBasePlate(0, 0, 5);
        scene.title("wires", "Wires");
        scene.addKeyframe();
        scene.world.showSection(util.select.layer(0), Direction.UP);
        scene.rotateCameraY(35);
        scene.idle(10);

        scene.world.showSection(util.select.column(3, 2), Direction.DOWN);

        scene.idle(10);

        scene.world.showSection(util.select.column(1, 2), Direction.DOWN);

        scene.idle(20);
        scene.overlay.showControls(new InputWindowElement(new Vec3(3.5, 1.6, 2.5), Pointing.DOWN)
                .withItem(NewAgeItems.GOLDEN_WIRE.asStack()), 20);

        scene.idle(30);

        scene.overlay.showControls(new InputWindowElement(new Vec3(1.5, 1.6, 2.5), Pointing.DOWN)
                .withItem(NewAgeItems.GOLDEN_WIRE.asStack()), 20);

        scene.idle(10);

        scene.world.modifyBlockEntity(new BlockPos(1, 1, 2), ElectricalConnectorBlockEntity.class, ElectricityPonder::connect);

        scene.idle(30);

    }

    public static void connect(ElectricalConnectorBlockEntity en) {
        en.connect((ElectricalConnectorBlockEntity) en.getLevel().getBlockEntity(en.getBlockPos().offset(2, 0, 0)), WireType.GOLD);
    }

}
