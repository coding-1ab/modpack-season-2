package org.antarcticgardens.cna.content.electricity.battery;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.client.resources.model.BakedModel;
import org.antarcticgardens.cna.CNASpriteShifts;

public class BatteryModel extends CTModel {
    public BatteryModel(BakedModel originalModel) {
        super(originalModel, new FluidTankCTBehaviour(CNASpriteShifts.BATTERY_SIDE, CNASpriteShifts.BATTERY_TOP,
                AllSpriteShifts.FLUID_TANK_INNER));
    }
}
