package org.antarcticgardens.cna.compat.computercraft;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.registries.BuiltInRegistries;
import org.antarcticgardens.cna.compat.computercraft.peripherals.EnergiserBlockEntityPeripheral;
import org.antarcticgardens.cna.content.energising.EnergiserBlockEntity;

import java.util.function.Supplier;

public class CNAComputerBehaviour extends AbstractComputerBehaviour {
    SyncedPeripheral<?> peripheral;
    Supplier<SyncedPeripheral<?>> peripheralSupplier;

    public CNAComputerBehaviour(SmartBlockEntity be) {
        super(be);
        this.peripheralSupplier = getPeripheralSupplier(be);
    }

    public static Supplier<SyncedPeripheral<?>> getPeripheralSupplier(SmartBlockEntity be) {
        if (be instanceof EnergiserBlockEntity energiserBlockEntity)
            return () -> new EnergiserBlockEntityPeripheral(energiserBlockEntity);

        throw new IllegalArgumentException(
                "No peripheral registered for " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()));
    }

    @Override
    public IPeripheral getPeripheralCapability() {
        if (peripheral == null)
            peripheral = peripheralSupplier.get();
        return peripheral;
    }
}
