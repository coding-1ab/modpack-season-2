package com.simibubi.create.content.logistics.factoryBoard;

import net.createmod.catnip.utility.Pointing;
import net.minecraft.nbt.CompoundTag;

public record FactoryPanelConnection(Pointing fromSide) {
	
	public static FactoryPanelConnection read(CompoundTag nbt) {
		return new FactoryPanelConnection(Pointing.valueOf(nbt.getString("FromSide")));
	}

	public CompoundTag write() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("FromSide", fromSide.name());
		return nbt;
	}

}
