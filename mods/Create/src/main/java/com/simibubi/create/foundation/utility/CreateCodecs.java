package com.simibubi.create.foundation.utility;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.item.ItemSlots;

import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

public class CreateCodecs {
	public static final Codec<Integer> INT_STR = Codec.STRING.comapFlatMap(
		string -> {
			try {
				return DataResult.success(Integer.parseInt(string));
			} catch (NumberFormatException ignored) {
				return DataResult.error(() -> "Not an integer: " + string);
			}
		},
		String::valueOf
	);

	public static final Codec<ItemStackHandler> ITEM_STACK_HANDLER = ItemSlots.CODEC.xmap(
		slots -> slots.toHandler(ItemStackHandler::new), ItemSlots::fromHandler
	);

	/**
	 * Codec for a simple FluidTank with no validator support.
	 */
	public static final Codec<FluidTank> FLUID_TANK = RecordCodecBuilder.create(i -> i.group(
		FluidStack.CODEC.fieldOf("fluid").forGetter(FluidTank::getFluid),
		ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(FluidTank::getCapacity)
	).apply(i, (fluid, capacity) -> {
		FluidTank tank = new FluidTank(capacity);
		tank.setFluid(fluid);
		return tank;
	}));

	public static Codec<Integer> boundedIntStr(int min) {
		return ExtraCodecs.validate(
			INT_STR,
			i -> i >= min ? DataResult.success(i) : DataResult.error(() -> "Value under minimum of " + min)
		);
	}
}
