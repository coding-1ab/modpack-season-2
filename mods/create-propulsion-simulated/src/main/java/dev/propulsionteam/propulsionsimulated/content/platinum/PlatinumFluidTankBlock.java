package dev.propulsionteam.propulsionsimulated.content.platinum;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PlatinumFluidTankBlock extends FluidTankBlock {
    public PlatinumFluidTankBlock(Properties properties) {
        super(properties, false);
    }

    @Override
    public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
        return PropulsionBlockEntities.PLATINUM_FLUID_TANK_BLOCK_ENTITY.get();
    }
}
