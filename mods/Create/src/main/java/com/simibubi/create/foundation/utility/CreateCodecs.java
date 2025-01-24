package com.simibubi.create.foundation.utility;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import com.simibubi.create.foundation.item.ItemSlots;

import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.util.ExtraCodecs;

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

	public static Codec<Integer> boundedIntStr(int min) {
		return ExtraCodecs.validate(
			INT_STR,
			i -> i >= min ? DataResult.success(i) : DataResult.error(() -> "Value under minimum of " + min)
		);
	}
}
