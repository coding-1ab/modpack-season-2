package com.simibubi.create.content.contraptions;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import org.jetbrains.annotations.Nullable;

public class MountedStorageInteraction {
	public static final List<MenuType<?>> GENERIC_MENUS = ImmutableList.of(
		MenuType.GENERIC_9x1, MenuType.GENERIC_9x2, MenuType.GENERIC_9x3,
		MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6
	);

	@Nullable
	public static MenuProvider createMenuProvider(Component menuName, IItemHandlerModifiable handler,
												  int rows, Predicate<Player> stillValid) {
		if (rows < 1 || rows > 6)
			return null;

		MenuType<?> menuType = GENERIC_MENUS.get(rows - 1);

		return new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
				StorageInteractionContainer wrapper = new StorageInteractionContainer(handler, stillValid);
				return new ChestMenu(menuType, id, inventory, wrapper, rows);
			}

			@Override
			public Component getDisplayName() {
				return menuName;
			}
		};
	}

	public static class StorageInteractionContainer extends RecipeWrapper {
		private final Predicate<Player> stillValid;

		public StorageInteractionContainer(IItemHandlerModifiable inv, Predicate<Player> stillValid) {
			super(inv);
			this.stillValid = stillValid;
		}

		@Override
		public boolean stillValid(Player player) {
			return stillValid.test(player);
		}

		@Override
		public int getMaxStackSize() {
			return 64;
		}
	}

}
