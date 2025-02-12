package com.simibubi.create.foundation.item;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.AttachedRegistry;

import net.minecraft.world.item.Item;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;

@FunctionalInterface
public interface TooltipModifier {
	AttachedRegistry<Item, TooltipModifier> REGISTRY = AttachedRegistry.create();

	TooltipModifier EMPTY = new TooltipModifier() {
		@Override
		public void modify(ItemTooltipEvent context) {
		}

		@Override
		public TooltipModifier andThen(TooltipModifier after) {
			return after;
		}
	};

	void modify(ItemTooltipEvent context);

	default TooltipModifier andThen(TooltipModifier after) {
		if (after == EMPTY) {
			return this;
		}
		return tooltip -> {
			modify(tooltip);
			after.modify(tooltip);
		};
	}

	static TooltipModifier mapNull(@Nullable TooltipModifier modifier) {
		if (modifier == null) {
			return EMPTY;
		}
		return modifier;
	}
}
