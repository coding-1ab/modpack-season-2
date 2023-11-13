package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class StockTickerRequestScreen extends AbstractSimiScreen {

	private StockTickerBlockEntity blockEntity;

	private int ticksSinceLastUpdate = 0;

	private LerpedFloat itemScroll = LerpedFloat.linear()
		.startWithValue(0);

	public StockTickerRequestScreen(StockTickerBlockEntity be) {
		super(be.getBlockState()
			.getBlock()
			.getName());
		this.blockEntity = be;
		be.lastClientsideStockSnapshot = null;
		ticksSinceLastUpdate = 15;
	}

	@Override
	public void tick() {
		super.tick();

		itemScroll.tickChaser();
		if (Math.abs(itemScroll.getValue() - itemScroll.getChaseTarget()) < 1 / 16f)
			itemScroll.setValue(itemScroll.getChaseTarget());

		if (ticksSinceLastUpdate < 15) {
			ticksSinceLastUpdate++;
			return;
		}
		ticksSinceLastUpdate = 0;
		blockEntity.refreshClientStockSnapshot();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		List<IntAttached<ItemStack>> items = blockEntity.getClientStockSnapshot();
		int size = items == null ? 0 : items.size();

		int rows = 8;
		int cols = 8;

		float newTarget = itemScroll.getChaseTarget() + (int) Math.signum(-delta);
		newTarget = Mth.clamp(newTarget, 0, Math.max(0, Mth.ceil(size / (1.0 * cols)) - rows));
		itemScroll.chase(newTarget, 0.5, Chaser.EXP);

		return true;
	}

	@Override
	protected void init() {
		setWindowSize(256, 256);
		super.init();
	}

	private int getHoveredSlot(int x, int y) {
		List<IntAttached<ItemStack>> items = blockEntity.getClientStockSnapshot();

		int rows = 8;
		int cols = 8;
		int rowHeight = 18;
		int colWidth = 26;
		int itemsX = guiLeft + (windowWidth - cols * colWidth) / 2;
		int itemsY = guiTop + 45;

		if (items == null)
			return -1;
		if (x < itemsX || x >= itemsX + cols * colWidth)
			return -1;
		if (y < itemsY || y >= itemsY + rows * rowHeight)
			return -1;
		if (!itemScroll.settled())
			return -1;
		int row = (y - itemsY) / rowHeight + (int) itemScroll.getChaseTarget();
		int col = (x - itemsX) / colWidth;
		int slot = row * cols + col;
		if (items.size() <= slot)
			return -1;
		return slot;
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		List<IntAttached<ItemStack>> items = blockEntity.getClientStockSnapshot();
		PoseStack ms = graphics.pose();

		int rows = 8;
		int cols = 8;
		int rowHeight = 18;
		int colWidth = 26;
		int x = guiLeft + (windowWidth - cols * colWidth) / 2;
		int y = guiTop + 45;

		float currentScroll = itemScroll.getValue(minecraft.getDeltaFrameTime());
		int startRow = Math.max(0, Mth.floor(currentScroll) - 1);

		ms.pushPose();
		ms.translate(0, -currentScroll * rowHeight, 0);
		int hoveredSlot = getHoveredSlot(mouseX, mouseY);

		if (items == null) {
			return;
		}

		for (int row = startRow; row < startRow + rows + 2; row++) {
			float scale = 1;
			if (row < currentScroll)
				scale = Mth.clamp(1 - (currentScroll - row), 0, 1);
			if (row > currentScroll + (rows - 1))
				scale = Mth.clamp((currentScroll + rows) - row, 0, 1);
			scale *= scale;
			if (scale < 0.1)
				continue;

			for (int col = 0; col < cols; col++) {
				int index = row * cols + col;
				if (items.size() <= index)
					break;
				ms.pushPose();
				ms.translate(x + col * colWidth, y + row * rowHeight, 0);
				float scaleFromHover = 1;
				ms.translate(0, 0, 200);

				IntAttached<ItemStack> entry = items.get(index);
				int count = entry.getFirst();
				if (count > 1) {
					String text = count >= 1000000 ? (count / 1000000) + "m"
						: count >= 10000 ? (count / 1000) + "k"
							: count >= 1000 ? ((count * 10) / 1000) / 10f + "k" : count >= 100 ? count + "" : " " + count;
					for (int xi : Iterate.positiveAndNegative)
						graphics.drawString(font, Lang.text(text)
							.component(), 11 + xi, 10, xi < 0 ? 0x555555 : 0x333333, false);
					for (int yi : Iterate.positiveAndNegative)
						graphics.drawString(font, Lang.text(text)
							.component(), 11, 10 + yi, yi < 0 ? 0x555555 : 0x333333, false);
					graphics.drawString(font, Lang.text(text)
						.component(), 11, 10, 0xffffff, false);
				}

				ms.translate(0, 0, -200);

				if (index == hoveredSlot) {
					Color color2 = new Color(255, 255, 255, 50);
					ms.translate(-.5f, -.5f, 0);
					graphics.renderOutline(0, 0, colWidth - 1, rowHeight - 1, color2.getRGB());
					ms.translate(.5f, .5f, 0);
					scaleFromHover += .05f;
				}

				ms.translate((colWidth - 18) / 2.0, (rowHeight - 18) / 2.0, 0);
				ms.translate(18 / 2.0, 18 / 2.0, 0);
				ms.scale(scale, scale, scale);
				ms.scale(scaleFromHover, scaleFromHover, scaleFromHover);
				ms.translate(-18 / 2.0, -18 / 2.0, 0);
				GuiGameElement.of(entry.getSecond())
					.render(graphics);
				ms.popPose();
			}
		}

		ms.popPose();

		int totalHeight = rows * rowHeight;
		int allRows = Mth.ceil(items.size() / (1.0 * cols));
		int barSize = Mth.floor(1f * totalHeight * rows / allRows);
		if (barSize < totalHeight) {
			ms.pushPose();
			ms.translate(0, currentScroll * (totalHeight - barSize) / Math.max(1, allRows - rows), 0);
			Color color = new Color(255, 255, 255, 50);
			graphics.renderOutline(x + cols * colWidth + 3, y, 1, barSize, color.getRGB());
			ms.popPose();
		}

	}

}
