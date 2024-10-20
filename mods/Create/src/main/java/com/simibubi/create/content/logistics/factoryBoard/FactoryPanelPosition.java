package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;

public record FactoryPanelPosition(BlockPos pos, PanelSlot slot) {

	public static FactoryPanelPosition read(CompoundTag nbt) {
		return new FactoryPanelPosition(NbtUtils.readBlockPos(nbt),
			PanelSlot.values()[Mth.positiveModulo(nbt.getInt("Slot"), 4)]);
	}

	public CompoundTag write() {
		CompoundTag nbt = NbtUtils.writeBlockPos(pos);
		nbt.putInt("Slot", slot.ordinal());
		return nbt;
	}

}
