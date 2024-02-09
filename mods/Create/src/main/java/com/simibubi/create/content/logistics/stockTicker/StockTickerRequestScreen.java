package com.simibubi.create.content.logistics.stockTicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class StockTickerRequestScreen extends AbstractSimiScreen {

	StockTickerBlockEntity blockEntity;
	int ticksSinceLastUpdate = 0;
	LerpedFloat itemScroll;

	final int rows = 8;
	final int cols = 8;
	final int rowHeight = 18;
	final int colWidth = 26;
	final int noneHovered = -1;
	int itemsX;
	int itemsY;
	int orderY;

	EditBox searchBox;
	EditBox addressBox;

	int emptyTicks = 0;
	int successTicks = 0;

	List<IntAttached<ItemStack>> currentItemSource;
	List<IntAttached<ItemStack>> displayedItems;
	List<IntAttached<ItemStack>> itemsToOrder;

	public StockTickerRequestScreen(StockTickerBlockEntity be) {
		super(be.getBlockState()
			.getBlock()
			.getName());
		displayedItems = new ArrayList<>();
		itemsToOrder = new ArrayList<>();
		blockEntity = be;
		blockEntity.lastClientsideStockSnapshot = null;
		ticksSinceLastUpdate = 15;
		emptyTicks = 0;
		successTicks = 0;
		itemScroll = LerpedFloat.linear()
			.startWithValue(0);
	}

	@Override
	protected void init() {
		setWindowSize(256, 256);
		super.init();

		itemsX = guiLeft + (windowWidth - cols * colWidth) / 2;
		itemsY = guiTop + 35;
		orderY = itemsY + rows * rowHeight + 10;

		MutableComponent searchLabel = Lang.translateDirect("gui.stock_ticker.search_items");
		searchBox = new EditBox(this.font, itemsX + 1, itemsY - 18, 120, 9, searchLabel);
		searchBox.setMaxLength(50);
		searchBox.setBordered(false);
		searchBox.setTextColor(0xffffff);
		addRenderableWidget(searchBox);

		MutableComponent addressLabel = Lang.translateDirect("gui.stock_ticker.package_adress");
		boolean initial = addressBox == null;
		addressBox = new EditBox(this.font, itemsX + 1, orderY + rowHeight + 10, 120, 9, addressLabel);
		addressBox.setMaxLength(50);
		addressBox.setBordered(false);
		addressBox.setTextColor(0xffffff);
		if (initial)
			addressBox.setValue(blockEntity.previouslyUsedAddress);
		addRenderableWidget(addressBox);
	}

	private void refreshSearchResults(boolean scrollBackUp) {
		displayedItems = Collections.emptyList();
		if (scrollBackUp)
			itemScroll.startWithValue(0);

		if (currentItemSource == null) {
			clampScrollBar();
			return;
		}

		String valueWithPrefix = searchBox.getValue();
		if (valueWithPrefix.isBlank()) {
			displayedItems = currentItemSource;
			clampScrollBar();
			return;
		}

		boolean modSearch = false;
		boolean tagSearch = false;
		if ((modSearch = valueWithPrefix.startsWith("@")) || (tagSearch = valueWithPrefix.startsWith("#")))
			valueWithPrefix = valueWithPrefix.substring(1);
		final String value = valueWithPrefix;

		displayedItems = new ArrayList<>();
		for (IntAttached<ItemStack> entry : currentItemSource) {
			ItemStack stack = entry.getValue();

			if (modSearch) {
				if (ForgeRegistries.ITEMS.getKey(stack.getItem())
					.getNamespace()
					.contains(value)) {
					displayedItems.add(entry);
				}
				continue;
			}

			if (tagSearch) {
				if (stack.getTags()
					.anyMatch(key -> key.location()
						.toString()
						.contains(value)))
					displayedItems.add(entry);
				continue;
			}

			if (stack.getHoverName()
				.getString()
				.contains(value)
				|| ForgeRegistries.ITEMS.getKey(stack.getItem())
					.getPath()
					.contains(value)) {
				displayedItems.add(entry);
				continue;
			}
		}

		clampScrollBar();
	}

	@Override
	public void tick() {
		super.tick();

		if (displayedItems.isEmpty())
			emptyTicks++;
		else
			emptyTicks = 0;
		
		if (successTicks > 0 && itemsToOrder.isEmpty())
			successTicks++;
		else
			successTicks = 0;

		List<IntAttached<ItemStack>> clientStockSnapshot = blockEntity.getClientStockSnapshot();
		if (clientStockSnapshot != currentItemSource) {
			currentItemSource = clientStockSnapshot;
			refreshSearchResults(false);
			revalidateOrders();
		}

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
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack ms = graphics.pose();
		Color color = new Color(255, 255, 255, 50);
		float currentScroll = itemScroll.getValue(partialTicks);
		int startRow = Math.max(0, Mth.floor(currentScroll) - 1);
		int hoveredSlot = getHoveredSlot(mouseX, mouseY, true);

		// Render some boxes
		graphics.renderOutline(searchBox.getX() - 4, searchBox.getY() - 4, searchBox.getWidth() + 12,
			searchBox.getHeight() + 7, color.getRGB());
		graphics.renderOutline(addressBox.getX() - 4, addressBox.getY() - 4, addressBox.getWidth() + 12,
			addressBox.getHeight() + 7, color.getRGB());
		graphics.renderOutline(itemsX - 3, itemsY - 4, cols * colWidth + 6, rows * rowHeight + 8, color.getRGB());
		graphics.renderOutline(itemsX - 3, orderY - 4, cols * colWidth + 6, rowHeight + 8, color.getRGB());
		drawConfirmBox(graphics, color, mouseX, mouseY);

		// Render text input hints
		if (searchBox.getValue()
			.isBlank())
			graphics.drawString(font, searchBox.getMessage(), searchBox.getX(), searchBox.getY(), 0x88dddddd);
		if (addressBox.getValue()
			.isBlank())
			graphics.drawString(font, addressBox.getMessage(), addressBox.getX(), addressBox.getY(), 0x88dddddd);

		// Render static item icons
		ms.pushPose();
		ms.translate(itemsX + cols * colWidth + 3, orderY - 8, 0);
		ms.scale(2f, 2f, 2f);
		GuiGameElement.of(AllItems.CARDBOARD_PACKAGE_10x12.asStack())
			.render(graphics);
		ms.popPose();

		ms.pushPose();
		ms.translate(itemsX + cols * colWidth + 8, itemsY, 0);
		ms.scale(1.5f, 1.5f, 1.5f);
		GuiGameElement.of(AllBlocks.PACKAGER.asStack())
			.render(graphics);
		ms.translate(0, -9, 15);
		GuiGameElement.of(AllBlocks.PACKAGER_LINK.asStack())
			.render(graphics);
		ms.popPose();
		graphics.drawString(font, Lang.text(blockEntity.activeLinks + "")
			.component(), itemsX + cols * colWidth + 33, itemsY + 11, 0x88dddddd);

		// Render ordered items
		for (int index = 0; index < cols; index++) {
			if (itemsToOrder.size() <= index)
				break;

			IntAttached<ItemStack> entry = itemsToOrder.get(index);
			boolean isStackHovered = index == hoveredSlot;

			ms.pushPose();
			ms.translate(itemsX + index * colWidth, orderY, 0);
			renderItemEntry(graphics, 1, entry, isStackHovered, true);
			ms.popPose();
		}

		// Something isnt right
		if (displayedItems.isEmpty()) {
			Component msg = getTroubleshootingMessage();
			float alpha = Mth.clamp((emptyTicks - 10f) / 5f, 0f, 1f);
			if (alpha > 0)
				graphics.drawString(font, msg, itemsX + 1, itemsY, new Color(.5f, .5f, .5f, alpha).getRGB());
		}
		
		// Request just sent
		if (itemsToOrder.isEmpty() && successTicks > 0) {
			Component msg = Lang.translateDirect("gui.stock_ticker.request_sent");
			float alpha = Mth.clamp((successTicks - 10f) / 5f, 0f, 1f);
			if (alpha > 0)
				graphics.drawCenteredString(font, msg, itemsX + cols * colWidth / 2, orderY + 4, new Color(.75f, .95f, .75f, alpha).getRGB());
		}

		hoveredSlot = getHoveredSlot(mouseX, mouseY, false);

		ms.pushPose();
		ms.translate(0, -currentScroll * rowHeight, 0);

		// Render item pool
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
				if (displayedItems.size() <= index)
					break;

				IntAttached<ItemStack> entry = displayedItems.get(index);
				boolean isStackHovered = index == hoveredSlot;

				ms.pushPose();
				ms.translate(itemsX + col * colWidth, itemsY + row * rowHeight, 0);
				renderItemEntry(graphics, scale, entry, isStackHovered, false);
				ms.popPose();
			}
		}

		ms.popPose();

		// Scroll bar
		int totalHeight = rows * rowHeight;
		int allRows = Mth.ceil(displayedItems.size() / (1.0 * cols));
		int barSize = Mth.floor(1f * totalHeight * rows / allRows);
		if (barSize < totalHeight) {
			ms.pushPose();
			ms.translate(0, currentScroll * (totalHeight - barSize) / Math.max(1, allRows - rows), 0);
			graphics.renderOutline(itemsX + cols * colWidth + 2, itemsY, 1, barSize, color.getRGB());
			ms.popPose();
		}

		// Render tooltip of hovered item
		if (hoveredSlot != noneHovered)
			graphics.renderTooltip(font, displayedItems.get(hoveredSlot)
				.getValue(), mouseX, mouseY);
		hoveredSlot = getHoveredSlot(mouseX, mouseY, true);
		if (hoveredSlot != noneHovered)
			graphics.renderTooltip(font, itemsToOrder.get(hoveredSlot)
				.getValue(), mouseX, mouseY);
	}

	private void renderItemEntry(GuiGraphics graphics, float scale, IntAttached<ItemStack> entry,
		boolean isStackHovered, boolean isRenderingOrders) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(0, 0, 200);

		int customCount = entry.getFirst();
		if (!isRenderingOrders) {
			IntAttached<ItemStack> order = getOrderForItem(entry.getSecond());
			if (order != null)
				customCount -= order.getFirst();
		}

		drawItemCount(graphics, entry.getFirst(), customCount);
		ms.translate(0, 0, -200);

		float scaleFromHover = 1;
		if (isStackHovered)
			scaleFromHover += .075f;

		ms.translate((colWidth - 18) / 2.0, (rowHeight - 18) / 2.0, 0);
		ms.translate(18 / 2.0, 18 / 2.0, 0);
		ms.scale(scale, scale, scale);
		ms.scale(scaleFromHover, scaleFromHover, scaleFromHover);
		ms.translate(-18 / 2.0, -18 / 2.0, 0);
		GuiGameElement.of(entry.getSecond())
			.render(graphics);
		ms.popPose();
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
			graphics.drawString(font, Lang.text(text)
				.component(), 11 + xi, 10, xi < 0 ? lightOutline : darkOutline, false);
		for (int yi : Iterate.positiveAndNegative)
			graphics.drawString(font, Lang.text(text)
				.component(), 11, 10 + yi, yi < 0 ? lightOutline : darkOutline, false);
		graphics.drawString(font, Lang.text(text)
			.component(), 11, 10, middleColor, false);
	}

	@Nullable
	private IntAttached<ItemStack> getOrderForItem(ItemStack stack) {
		for (IntAttached<ItemStack> entry : itemsToOrder)
			if (ItemHandlerHelper.canItemStacksStack(stack, entry.getValue()))
				return entry;
		return null;
	}

	private void revalidateOrders() {
		Set<IntAttached<ItemStack>> invalid = new HashSet<>(itemsToOrder);
		if (currentItemSource == null) {
			itemsToOrder.removeAll(invalid);
			return;
		}
		for (IntAttached<ItemStack> entry : itemsToOrder)
			for (IntAttached<ItemStack> available : currentItemSource)
				if (ItemHandlerHelper.canItemStacksStack(entry.getValue(), available.getValue())) {
					entry.setFirst(Math.min(available.getFirst(), entry.getFirst()));
					invalid.remove(entry);
					break;
				}
		itemsToOrder.removeAll(invalid);
	}

	private int getHoveredSlot(int x, int y, boolean order) {
		if (x < itemsX || x >= itemsX + cols * colWidth)
			return noneHovered;
		if (!order && (y < itemsY || y >= itemsY + rows * rowHeight))
			return noneHovered;
		if (order && (y < orderY || y >= orderY + rowHeight))
			return noneHovered;
		if (!order && !itemScroll.settled())
			return noneHovered;

		int row = (y - itemsY) / rowHeight + (int) itemScroll.getChaseTarget();
		int col = (x - itemsX) / colWidth;
		int slot = row * cols + col;

		if (order && itemsToOrder.size() <= col)
			return noneHovered;
		if (!order && displayedItems.size() <= slot)
			return noneHovered;
		return order ? col : slot;
	}

	private void drawConfirmBox(GuiGraphics graphics, Color defaultColor, int mouseX, int mouseY) {
		int confirmX = addressBox.getX() + addressBox.getWidth() + 10;
		int confirmY = addressBox.getY() - 4;
		int confirmW = (cols * colWidth) - addressBox.getWidth() - 8;
		int confirmH = addressBox.getHeight() + 7;

		boolean hovered = isConfirmHovered(mouseX, mouseY);
		boolean inactive = itemsToOrder.isEmpty();
		int color = inactive ? defaultColor.darker()
			.getRGB() : hovered ? 0xeeffffff : 0x99ffffff;
		graphics.renderOutline(confirmX, confirmY, confirmW, confirmH, color);
		graphics.drawCenteredString(font, Lang.translateDirect("gui.stock_ticker.confirm_order"),
			confirmX + (confirmW / 2), confirmY + 4, color);
	}

	private boolean isConfirmHovered(int mouseX, int mouseY) {
		int confirmX = addressBox.getX() + addressBox.getWidth() + 10;
		int confirmY = addressBox.getY() - 4;
		int confirmW = (cols * colWidth) - addressBox.getWidth() - 8;
		int confirmH = addressBox.getHeight() + 7;

		if (mouseX < confirmX || mouseX >= confirmX + confirmW)
			return false;
		if (mouseY < confirmY || mouseY >= confirmY + confirmH)
			return false;
		return true;
	}

	private Component getTroubleshootingMessage() {
		if (currentItemSource == null)
			return Lang.translate("gui.stock_ticker.checking_stocks")
				.component();
		if (blockEntity.activeLinks == 0)
			return Lang.translate("gui.stock_ticker.no_packagers_linked")
				.component();
		if (currentItemSource.isEmpty())
			return Lang.translate("gui.stock_ticker.inventories_empty")
				.component();
		return Lang.translate("gui.stock_ticker.no_search_results")
			.component();
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		boolean orderClicked = false;
		int hoveredSlot = getHoveredSlot((int) pMouseX, (int) pMouseY, false);
		if (hoveredSlot == noneHovered) {
			hoveredSlot = getHoveredSlot((int) pMouseX, (int) pMouseY, true);
			orderClicked = true;
		}

		boolean lmb = pButton == 0;
		boolean rmb = pButton == 1;

		if (rmb && searchBox.isMouseOver(pMouseX, pMouseY)) {
			searchBox.setValue("");
			refreshSearchResults(true);
			searchBox.setFocused(true);
			return true;
		}

		if (lmb && isConfirmHovered((int) pMouseX, (int) pMouseY)) {
			sendIt();
			return true;
		}

		if (hoveredSlot == noneHovered || !lmb && !rmb)
			return super.mouseClicked(pMouseX, pMouseY, pButton);

		IntAttached<ItemStack> entry = orderClicked ? itemsToOrder.get(hoveredSlot) : displayedItems.get(hoveredSlot);
		ItemStack itemStack = entry.getValue();
		IntAttached<ItemStack> existingOrder = getOrderForItem(itemStack);
		if (existingOrder == null) {
			if (itemsToOrder.size() >= cols || rmb)
				return true;
			itemsToOrder.add(existingOrder = IntAttached.withZero(itemStack));
		}

		int transfer = hasShiftDown() ? itemStack.getMaxStackSize() : 1;
		int current = existingOrder.getFirst();

		if (rmb || orderClicked) {
			existingOrder.setFirst(current - transfer);
			if (existingOrder.getFirst() <= 0)
				itemsToOrder.remove(existingOrder);
			return true;
		}

		existingOrder.setFirst(current + Math.min(transfer, entry.getFirst() - current));
		return true;
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int maxScroll = Math.max(0, Mth.ceil(displayedItems.size() / (1.0 * cols)) - rows);
		int direction = (int) Math.signum(-delta);
		float newTarget = Mth.clamp(itemScroll.getChaseTarget() + direction, 0, maxScroll);
		itemScroll.chase(newTarget, 0.5, Chaser.EXP);
		return true;
	}

	private void clampScrollBar() {
		int maxScroll = Math.max(0, Mth.ceil(displayedItems.size() / (1.0 * cols)) - rows);
		float prevTarget = itemScroll.getChaseTarget();
		float newTarget = Mth.clamp(prevTarget, 0, maxScroll);
		if (prevTarget != newTarget)
			itemScroll.startWithValue(newTarget);
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		if (addressBox.isFocused() && addressBox.charTyped(pCodePoint, pModifiers))
			return true;
		String s = searchBox.getValue();
		if (!searchBox.charTyped(pCodePoint, pModifiers))
			return false;
		if (!Objects.equals(s, searchBox.getValue()))
			refreshSearchResults(true);
		return true;
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == GLFW.GLFW_KEY_ENTER && hasShiftDown()) {
			sendIt();
			return true;
		}

		if (addressBox.isFocused() && addressBox.keyPressed(pKeyCode, pScanCode, pModifiers))
			return true;

		String s = searchBox.getValue();
		if (!searchBox.keyPressed(pKeyCode, pScanCode, pModifiers))
			return searchBox.isFocused() && searchBox.isVisible() && pKeyCode != 256 ? true
				: super.keyPressed(pKeyCode, pScanCode, pModifiers);
		if (!Objects.equals(s, searchBox.getValue()))
			refreshSearchResults(true);
		return true;
	}

	@Override
	public void removed() {
		AllPackets.getChannel()
			.sendToServer(new PackageOrderRequestPacket(blockEntity.getBlockPos(), Collections.emptyList(),
				addressBox.getValue()));
		super.removed();
	}
	
	private void sendIt() {
		revalidateOrders();
		if (itemsToOrder.isEmpty())
			return;
		
		AllPackets.getChannel()
			.sendToServer(
				new PackageOrderRequestPacket(blockEntity.getBlockPos(), itemsToOrder, addressBox.getValue()));
		
		itemsToOrder = new ArrayList<>();
		ticksSinceLastUpdate = 10;
		successTicks = 1;
	}

	@Override
	public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
		return super.keyReleased(pKeyCode, pScanCode, pModifiers);
	}

}
