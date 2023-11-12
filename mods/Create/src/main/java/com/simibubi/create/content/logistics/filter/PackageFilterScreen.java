package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PackageFilterScreen extends AbstractFilterScreen<PackageFilterMenu> {

	private static final String PREFIX = "gui.package_filter.";

	public PackageFilterScreen(PackageFilterMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, AllGuiTextures.PACKAGE_FILTER);
	}

	@Override
	protected void init() {
		setWindowOffset(-11, 7);
		super.init();

		int x = leftPos;
		int y = topPos;
	}

	@Override
	protected void contentsCleared() {}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		return false;
	}

	@Override
	protected boolean isIndicatorOn(Indicator indicator) {
		return false;
	}

}
