package com.mrh0.createaddition.index;

import com.mrh0.createaddition.blocks.alternator.AlternatorBlock;
import com.mrh0.createaddition.blocks.alternator.AlternatorBlockEntity;
import com.mrh0.createaddition.blocks.connector.LargeConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallLightConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlockEntity;
import com.mrh0.createaddition.blocks.creative_energy.CreativeEnergyBlockEntity;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlockEntity;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlockEntity;
import com.mrh0.createaddition.energy.IEnergyProvider;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;

public class CACapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        AlternatorBlockEntity.registerCapabilities(event);
        LargeConnectorBlockEntity.registerCapabilities(event);
        SmallConnectorBlockEntity.registerCapabilities(event);
        SmallLightConnectorBlockEntity.registerCapabilities(event);
        ElectricMotorBlockEntity.registerCapabilities(event);
        CreativeEnergyBlockEntity.registerCapabilities(event);
        ModularAccumulatorBlockEntity.registerCapabilities(event);
        PortableEnergyInterfaceBlockEntity.registerCapabilities(event);
        TeslaCoilBlockEntity.registerCapabilities(event);
    }
}
