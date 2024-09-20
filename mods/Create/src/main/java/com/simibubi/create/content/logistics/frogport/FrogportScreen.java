package com.simibubi.create.content.logistics.frogport;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FrogportScreen extends AbstractSimiScreen {

	private ItemStack renderedItem = AllBlocks.PACKAGE_FROGPORT.asStack();
	private ItemStack renderedPackage = PackageItem.containing(List.of());
	private AllGuiTextures background;
	private FrogportBlockEntity blockEntity;
	private EditBox addressBox;
	private IconButton confirmButton;
	private IconButton dontAcceptPackages;
	private IconButton dumpPackagesButton;

	public FrogportScreen(FrogportBlockEntity be) {
		super(AllBlocks.PACKAGE_FROGPORT.asStack()
			.getHoverName());
		background = AllGuiTextures.PACKAGE_FILTER;
		this.blockEntity = be;
	}

	@Override
	protected void init() {
		setWindowSize(background.getWidth(), background.getHeight());
		setWindowOffset(-11, 7);
		super.init();
		clearWidgets();

		int x = guiLeft;
		int y = guiTop;

		addressBox = new EditBox(this.font, x + 44, y + 28, 140, 9, Component.empty());
		addressBox.setMaxLength(50);
		addressBox.setBordered(false);
		addressBox.setTextColor(0xffffff);
		addressBox.setValue(blockEntity.addressFilter);
		if (!blockEntity.acceptsPackages)
			addressBox.setValue(CreateLang.translate("gui.package_port.accept_nothing")
				.string());

		addRenderableWidget(addressBox);

		confirmButton =
			new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.setScreen(null));
		addRenderableWidget(confirmButton);

		dumpPackagesButton = new IconButton(x + 156, y + background.getHeight() - 24, AllIcons.I_PRIORITY_LOW);
		dumpPackagesButton.withCallback(() -> AllPackets.getChannel()
			.sendToServer(new FrogportConfigurationPacket(blockEntity.getBlockPos(), addressBox.getValue(),
				!sendOnly(), true)));
		dumpPackagesButton.setToolTip(CreateLang.translateDirect("gui.package_port.eject_to_inventory"));
		addRenderableWidget(dumpPackagesButton);

		dontAcceptPackages = new IconButton(x + 15, y + background.getHeight() - 24, AllIcons.I_SEND_ONLY);
		dontAcceptPackages.withCallback(() -> {
			addressBox.setValue(CreateLang.translate("gui.package_port.accept_nothing")
				.string());
		});
		dontAcceptPackages.setToolTip(CreateLang.translateDirect("gui.package_port.send_only"));
		addRenderableWidget(dontAcceptPackages);

		setFocused(addressBox);
		tick();
	}

	@Override
	public void removed() {
		AllPackets.getChannel()
			.sendToServer(new FrogportConfigurationPacket(blockEntity.getBlockPos(), addressBox.getValue(),
				!sendOnly(), false));
		super.removed();
	}

	private boolean sendOnly() {
		return CreateLang.translate("gui.package_port.accept_nothing")
			.string()
			.equals(addressBox.getValue());
	}

	@Override
	public void tick() {
		super.tick();
		dontAcceptPackages.active = !sendOnly();
		if (dumpPackagesButton.active != (getPackageCount() != 0)) {
			dumpPackagesButton.active = !dumpPackagesButton.active;
			dumpPackagesButton.getToolTip()
				.clear();
			if (dumpPackagesButton.active)
				dumpPackagesButton.setToolTip(CreateLang.translateDirect("gui.package_port.eject_to_inventory"));
		}
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(graphics, x, y);
		graphics.drawString(font, title, x + background.getWidth() / 2 - font.width(title) / 2, y + 4, 0x3D3C48, false);

		GuiGameElement.of(renderedItem).<GuiGameElement
			.GuiRenderBuilder>at(x + background.getWidth() + 6, y + background.getHeight() - 56, -200)
			.scale(5)
			.render(graphics);

		AllGuiTextures.PACKAGE_PORT_SLOT.render(graphics, x + 136, y + background.getHeight() - 24);
		graphics.renderItem(renderedPackage, x + 16, y + 23);

		if (getPackageCount() > 0) {
			graphics.renderItem(renderedPackage, x + 137, y + background.getHeight() - 24);
			graphics.renderItemDecorations(font, renderedPackage, x + 137, y + background.getHeight() - 24,
				String.valueOf(getPackageCount()));

			if (mouseX > x + 136 && mouseX < x + 136 + 18 && mouseY > y + background.getHeight() - 24
				&& mouseY < y + background.getHeight() - 24 + 18)
				graphics.renderComponentTooltip(font,
					List.of(CreateLang.translate("gui.package_port.packages_backed_up", getPackageCount())
						.component()),
					mouseX, mouseY);
		}

		if (addressBox.isHovered()) {
			graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.package_port.catch_packages")
				.color(AbstractSimiWidget.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.package_port.catch_packages_empty")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.package_port.catch_packages_wildcard")
					.style(ChatFormatting.GRAY)
					.component()),
				mouseX, mouseY);
		}
	}

	private int getPackageCount() {
		int packageCount = 0;
		for (int i = 0; i < blockEntity.inventory.getSlots(); i++)
			if (!blockEntity.inventory.getStackInSlot(i)
				.isEmpty())
				packageCount++;
		return packageCount;
	}

}
