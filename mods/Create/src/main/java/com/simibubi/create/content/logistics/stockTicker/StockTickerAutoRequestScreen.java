package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class StockTickerAutoRequestScreen extends AbstractSimiScreen {

	StockTickerBlockEntity blockEntity;

	EditBox addressBox;
	Button takeSnapshotButton;

	List<Integer> modifiedAmounts;

	public StockTickerAutoRequestScreen(StockTickerBlockEntity be) {
		super(be.getBlockState()
			.getBlock()
			.getName());
		blockEntity = be;
		blockEntity.lastClientsideStockSnapshot = null;
		modifiedAmounts = null;
	}

	private void resetAmounts() {
		modifiedAmounts = new ArrayList<>();
		for (IntAttached<ItemStack> intAttached : currentStacks())
			modifiedAmounts.add(intAttached.getFirst());
	}

	@Override
	protected void init() {
		setWindowSize(256, 128);
		super.init();

		int x = guiLeft;
		int y = guiTop;

		takeSnapshotButton = Button.builder(Components.literal("Take Snapshot"), this::onPress)
			.bounds(x, y + 44, 100, 20)
			.build();

		MutableComponent addressLabel = CreateLang.translateDirect("gui.stock_ticker.package_adress");
		boolean initial = addressBox == null;
		addressBox = new EditBox(this.font, x, y + 80, 120, 9, addressLabel);
		addressBox.setMaxLength(50);
		addressBox.setBordered(false);
		addressBox.setTextColor(0xffffff);
		if (initial)
			addressBox.setValue(blockEntity.restockAddress);
		addRenderableWidget(addressBox);
		addRenderableWidget(takeSnapshotButton);
	}

	private void onPress(Button button) {
		if (button == takeSnapshotButton) {
			AllPackets.getChannel()
				.sendToServer(
					new StockTickerConfigurationPacket(blockEntity.getBlockPos(), true, "", PackageOrder.empty()));
			modifiedAmounts = null;
		}
	}

	@Override
	public void removed() {
		if (modifiedAmounts != null)
			for (int i = 0; i < currentStacks().size(); i++)
				currentStacks().get(i)
					.setFirst(modifiedAmounts.get(i));
		AllPackets.getChannel()
			.sendToServer(new StockTickerConfigurationPacket(blockEntity.getBlockPos(), false, addressBox.getValue(),
				blockEntity.restockAmounts));
		super.removed();
	}

	final int rows = 8;
	final int cols = 8;
	final int rowHeight = 18;
	final int colWidth = 26;

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (modifiedAmounts != null && currentStacks().size() != modifiedAmounts.size())
			resetAmounts();

		Color color = new Color(255, 255, 255, 50);
		int hoveredSlot = getHoveredSlot(mouseX, mouseY);

		// Render some boxes
		graphics.renderOutline(addressBox.getX() - 4, addressBox.getY() - 4, addressBox.getWidth() + 12,
			addressBox.getHeight() + 7, color.getRGB());
		graphics.renderOutline(guiLeft - 3, guiTop - 4 + 20, cols * colWidth + 6, rowHeight + 8, color.getRGB());

		// Render text input hints
		if (addressBox.getValue()
			.isBlank())
			graphics.drawString(font, addressBox.getMessage(), addressBox.getX(), addressBox.getY(), 0x88dddddd);

		graphics.drawString(font, Components.literal("Target Amounts:"), guiLeft, guiTop, 0x88dddddd);
		PoseStack ms = graphics.pose();

		for (int i = 0; i < currentStacks().size(); i++) {
			IntAttached<ItemStack> entry = currentStacks().get(i);
			ms.pushPose();

			ms.translate(guiLeft + i % cols * colWidth, guiTop + 20 + i / cols * rowHeight, 200);

			int customCount = modifiedAmounts == null ? currentStacks().get(i)
				.getFirst() : modifiedAmounts.get(i);
			drawItemCount(graphics, customCount, customCount);
			ms.translate(0, 0, -200);

			float scaleFromHover = hoveredSlot == i ? 1.075f : 1;
			ms.translate((colWidth - 18) / 2.0, (rowHeight - 18) / 2.0, 0);
			ms.translate(18 / 2.0, 18 / 2.0, 0);
			ms.scale(scaleFromHover, scaleFromHover, scaleFromHover);
			ms.translate(-18 / 2.0, -18 / 2.0, 0);
			GuiGameElement.of(entry.getSecond())
				.render(graphics);
			ms.popPose();
		}

		if (hoveredSlot != -1)
			graphics.renderTooltip(font, currentStacks().get(hoveredSlot)
				.getValue(), mouseX, mouseY);
	}

	private List<IntAttached<ItemStack>> currentStacks() {
		return blockEntity.restockAmounts.stacks();
	}

	private int getHoveredSlot(int mouseX, int mouseY) {
		if (mouseY < guiTop + 20 || mouseY > guiTop + 20 + rowHeight)
			return -1;
		int slot = (mouseX - guiLeft) / colWidth;
		if (slot >= currentStacks().size())
			return -1;
		return Math.max(-1, slot);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (modifiedAmounts == null)
			resetAmounts();
		int hoveredSlot = getHoveredSlot(Mth.floor(pMouseX), Mth.floor(pMouseY));
		if (hoveredSlot != -1) {
			int amount = modifiedAmounts.get(hoveredSlot);
			amount += (hasShiftDown() ? 64 : 1) * Math.signum(pDelta);
			if (hasShiftDown())
				amount = 64 * (amount / 64);
			amount = Math.max(amount, 1);
			modifiedAmounts.set(hoveredSlot, amount);
			return true;
		}

		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	private void drawItemCount(GuiGraphics graphics, int count, int customCount) {
		boolean special = customCount != count;
		if (!special && count == 1)
			return;

		count = customCount;
		String text = count >= 1000000 ? (count / 1000000) + "m"
			: count >= 10000 ? (count / 1000) + "k"
				: count >= 1000 ? ((count * 10) / 1000) / 10f + "k"
					: count >= 100 ? count + "" : count > 0 ? " " + count : " \u2714";

		int lightOutline = 0x444444;
		int darkOutline = 0x222222;
		int middleColor = special ? 0xaaffaa : 0xdddddd;

		for (int xi : Iterate.positiveAndNegative)
			graphics.drawString(font, CreateLang.text(text)
				.component(), 11 + xi, 10, xi < 0 ? lightOutline : darkOutline, false);
		for (int yi : Iterate.positiveAndNegative)
			graphics.drawString(font, CreateLang.text(text)
				.component(), 11, 10 + yi, yi < 0 ? lightOutline : darkOutline, false);
		graphics.drawString(font, CreateLang.text(text)
			.component(), 11, 10, middleColor, false);
	}

}
