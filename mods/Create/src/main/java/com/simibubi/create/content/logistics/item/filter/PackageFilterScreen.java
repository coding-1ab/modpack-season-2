package com.simibubi.create.content.logistics.item.filter;

import com.mojang.blaze3d.vertex.PoseStack;
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
	public void renderForeground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderForeground(matrixStack, mouseX, mouseY, partialTicks);
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
