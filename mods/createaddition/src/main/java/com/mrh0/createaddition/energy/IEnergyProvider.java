package com.mrh0.createaddition.energy;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public interface IEnergyProvider {
    IEnergyStorage getEnergyStorage(@Nullable Direction direction);
}
