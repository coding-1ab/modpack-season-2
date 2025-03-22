package com.mrh0.createaddition.blocks.connector.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public enum ConnectorMode implements StringRepresentable {
	Push("push"),
	Pull("pull"),
	None("none"),
	Passive("passive");

	private String name;

	ConnectorMode(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public ConnectorMode getNext() {
		switch (this) {
			//case Passive:
			//	return None;
			case None:
				return Pull;
			case Pull:
				return Push;
			case Push:
				return None;
		}
		return None;
	}

	public MutableComponent getTooltip() {
		switch (this) {
			case Passive:
				return Component.translatable("createaddition.tooltip.energy.passive");
			case None:
				return Component.translatable("createaddition.tooltip.energy.none");
			case Pull:
				return Component.translatable("createaddition.tooltip.energy.pull");
			case Push:
				return Component.translatable("createaddition.tooltip.energy.push");
		}
		return Component.translatable("createaddition.tooltip.energy.none");
	}

	public boolean isActive() {
		return this == Push || this == Pull;
	}

	public static ConnectorMode test(Level level, BlockPos pos, Direction face) {
		BlockEntity be = level.getBlockEntity(pos);
		if(be == null) return None;
		IEnergyStorage energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, face);
		if (energy == null) energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
		if (energy == null) return None;

		// if(e.canExtract() && e.canReceive()) return Passive;
		if(energy.canExtract()) return Pull;
		if(energy.canReceive()) return Push;

		return None;
	}
}
