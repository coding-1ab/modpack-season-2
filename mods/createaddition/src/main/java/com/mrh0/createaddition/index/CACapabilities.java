package com.mrh0.createaddition.index;

import com.mrh0.createaddition.energy.IEnergyProvider;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;

public class CACapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CABlockEntities.ALTERNATOR.get(),
                IEnergyProvider::getEnergyStorage
        );
    }
}
