package com.simibubi.create.foundation.blockEntity.behaviour;

import java.util.List;

import net.minecraft.network.chat.Component;

public record ValueSettingsBoard(Component title, int minValue, int maxValue, int milestoneInterval,
								 List<Component> rows,
	ValueSettingsFormatter formatter) {
	public ValueSettingsBoard(Component title, int maxValue, int milestoneInterval, List<Component> rows, ValueSettingsFormatter formatter) {
		this(title, 0, maxValue, milestoneInterval, rows, formatter);
	}
}
