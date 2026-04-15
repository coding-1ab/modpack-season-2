package dev.simulated_team.simulated.service.compat;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

public interface SimPeripheralService {

    <T extends BlockEntity> void addPeripheral(Supplier<BlockEntityType<T>> typeSupplier, Function<T, IPeripheral> peripheralFunction);
}
