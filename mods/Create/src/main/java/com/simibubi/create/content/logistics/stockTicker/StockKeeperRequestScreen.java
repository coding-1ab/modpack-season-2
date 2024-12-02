package com.simibubi.create.content.logistics.stockTicker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerRenderer;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class StockKeeperRequestScreen extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
	implements ScreenWithStencils {

	private static final AllGuiTextures NUMBERS = AllGuiTextures.NUMBERS;
	private static final AllGuiTextures HEADER = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER;
	private static final AllGuiTextures BODY = AllGuiTextures.STOCK_KEEPER_REQUEST_BODY;
	private static final AllGuiTextures FOOTER = AllGuiTextures.STOCK_KEEPER_REQUEST_FOOTER;

	StockTickerBlockEntity blockEntity;
	public LerpedFloat itemScroll;

	final int rows = 9;
	final int cols = 9;
	final int rowHeight = 20;
	final int colWidth = 20;
	final Couple<Integer> noneHovered = Couple.create(-1, -1);
	int itemsX;
	int itemsY;
	int orderY;
	int lockX;
	int lockY;
	int windowWidth;
	int windowHeight;

	public EditBox searchBox;
	EditBox addressBox;

	int emptyTicks = 0;
	int successTicks = 0;

	public List<List<BigItemStack>> currentItemSource;
	public List<List<BigItemStack>> displayedItems;
	public List<Pair<String, Integer>> categories;

	public List<BigItemStack> itemsToOrder;
	public List<BigItemStack> recipesToOrder;

	WeakReference<LivingEntity> stockKeeper;
	WeakReference<BlazeBurnerBlockEntity> blaze;

	boolean encodeRequester; // Redstone requesters
	ItemStack itemToProgram;

	private boolean isAdmin;
	private boolean isLocked;

	public boolean refreshSearchNextTick;
	private List<Rect2i> extraAreas = Collections.emptyList();

	public StockKeeperRequestScreen(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
		displayedItems = new ArrayList<>();
		itemsToOrder = new ArrayList<>();
		recipesToOrder = new ArrayList<>();
		categories = new ArrayList<>();
		isAdmin = menu.isAdmin;
		isLocked = menu.isLocked;
		blockEntity = container.contentHolder;
		blockEntity.lastClientsideStockSnapshot = null;
		blockEntity.ticksSinceLastUpdate = 15;
		emptyTicks = 0;
		successTicks = 0;
		itemScroll = LerpedFloat.linear()
			.startWithValue(0);
		stockKeeper = new WeakReference<>(null);
		blaze = new WeakReference<>(null);
		refreshSearchNextTick = false;
		menu.screenReference = this;

		itemToProgram = Minecraft.getInstance().player.getMainHandItem();
		encodeRequester =
			AllItemTags.TABLE_CLOTHS.matches(itemToProgram) || AllBlocks.REDSTONE_REQUESTER.isIn(itemToProgram);

		// Find the keeper for rendering
		for (int yOffset : Iterate.zeroAndOne) {
			for (Direction side : Iterate.horizontalDirections) {
				BlockPos seatPos = blockEntity.getBlockPos()
					.below(yOffset)
					.relative(side);
				for (SeatEntity seatEntity : blockEntity.getLevel()
					.getEntitiesOfClass(SeatEntity.class, new AABB(seatPos)))
					if (!seatEntity.getPassengers()
						.isEmpty()
						&& seatEntity.getPassengers()
							.get(0) instanceof LivingEntity keeper)
						stockKeeper = new WeakReference<>(keeper);
				if (yOffset == 0 && blockEntity.getLevel()
					.getBlockEntity(seatPos) instanceof BlazeBurnerBlockEntity bbbe) {
					blaze = new WeakReference<>(bbbe);
					return;
				}
			}
		}
	}

	@Override
	protected void init() {
		int appropriateHeight = Minecraft.getInstance()
			.getWindow()
			.getGuiScaledHeight() - 10;
		appropriateHeight -=
			Mth.positiveModulo(appropriateHeight - HEADER.getHeight() - FOOTER.getHeight(), BODY.getHeight());
		appropriateHeight =
			Math.min(appropriateHeight, HEADER.getHeight() + FOOTER.getHeight() + BODY.getHeight() * 17);

		setWindowSize(windowWidth = 226, windowHeight = appropriateHeight);
		super.init();
		clearWidgets();

		int x = getGuiLeft();
		int y = getGuiTop();

		itemsX = x + (windowWidth - cols * colWidth) / 2 + 1;
		itemsY = y + 33;
		orderY = y + windowHeight - 72;
		lockX = x + 186;
		lockY = y + 18;

		MutableComponent searchLabel = CreateLang.translateDirect("gui.stock_keeper.search_items");
		searchBox = new EditBox(new NoShadowFontWrapper(font), x + 71, y + 22, 100, 9, searchLabel);
		searchBox.setMaxLength(50);
		searchBox.setBordered(false);
		searchBox.setTextColor(0x4A2D31);
		addWidget(searchBox);

		boolean initial = addressBox == null;
		addressBox =
			new AddressEditBox(this, new NoShadowFontWrapper(font), x + 27, y + windowHeight - 36, 90, 10, true);
		addressBox.setTextColor(0x714A40);
		if (initial)
			addressBox.setValue(blockEntity.previouslyUsedAddress);
		addRenderableWidget(addressBox);

		extraAreas = new ArrayList<>();
		int leftHeight = 40;
		int rightHeight = 50;

		LivingEntity keeper = stockKeeper.get();
		if (keeper != null && keeper.isAlive())
			leftHeight = (int) (Math.max(0, keeper.getBoundingBox()
				.getYsize()) * 50);

		extraAreas.add(new Rect2i(0, y + windowHeight - 15 - leftHeight, x, height));
		if (encodeRequester)
			extraAreas.add(new Rect2i(x + windowWidth, y + windowHeight - 15 - rightHeight, rightHeight, rightHeight));
	}

	private void refreshSearchResults(boolean scrollBackUp) {
		displayedItems = Collections.emptyList();
		if (scrollBackUp)
			itemScroll.startWithValue(0);

		if (currentItemSource == null) {
			clampScrollBar();
			return;
		}

		boolean hasCraftables = hasCraftables();

		categories = new ArrayList<>();
		if (hasCraftables)
			categories.add(Pair.of(CreateLang.translate("gui.stock_keeper.jei_category")
				.string(), 0));

		blockEntity.categories.forEach(stack -> categories.add(Pair.of(stack.isEmpty() ? ""
			: stack.getHoverName()
				.getString(),
			0)));
		categories.add(Pair.of(CreateLang
			.translate(blockEntity.categories.isEmpty() ? "gui.stock_keeper.storage_category"
				: "gui.stock_keeper.unsorted_category")
			.string(), 0));

		String valueWithPrefix = searchBox.getValue();
		boolean anyItemsInCategory = false;
		int extraCategories = hasCraftables ? 1 : 0;

		// Nothing is being filtered out
		if (valueWithPrefix.isBlank()) {
			displayedItems = new ArrayList<>(currentItemSource);

			if (hasCraftables)
				displayedItems.add(0, getCraftables());

			int categoryY = 0;
			for (int categoryIndex = 0; categoryIndex < currentItemSource.size() + extraCategories; categoryIndex++) {
				categories.get(categoryIndex)
					.setSecond(categoryY);
				List<BigItemStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
				if (displayedItemsInCategory.isEmpty())
					continue;
				if (categoryIndex - extraCategories < currentItemSource.size() - 1)
					anyItemsInCategory = true;

				categoryY += rowHeight;
				categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
			}

			if (!anyItemsInCategory)
				categories.clear();

			clampScrollBar();
			return;
		}

		// Filter by search string
		boolean modSearch = false;
		boolean tagSearch = false;
		if ((modSearch = valueWithPrefix.startsWith("@")) || (tagSearch = valueWithPrefix.startsWith("#")))
			valueWithPrefix = valueWithPrefix.substring(1);
		final String value = valueWithPrefix;

		displayedItems = new ArrayList<>();
		currentItemSource.forEach($ -> displayedItems.add(new ArrayList<>()));

		if (hasCraftables)
			displayedItems.add(new ArrayList<>());

		int categoryY = 0;
		for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
			List<BigItemStack> category = categoryIndex - extraCategories < 0 ? getCraftables()
				: currentItemSource.get(categoryIndex - extraCategories);
			categories.get(categoryIndex)
				.setSecond(categoryY);

			if (displayedItems.size() <= categoryIndex)
				break;

			List<BigItemStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
			for (BigItemStack entry : category) {
				ItemStack stack = entry.stack;

				if (modSearch) {
					if (ForgeRegistries.ITEMS.getKey(stack.getItem())
						.getNamespace()
						.contains(value)) {
						displayedItemsInCategory.add(entry);
					}
					continue;
				}

				if (tagSearch) {
					if (stack.getTags()
						.anyMatch(key -> key.location()
							.toString()
							.contains(value)))
						displayedItemsInCategory.add(entry);
					continue;
				}

				if (stack.getHoverName()
					.getString()
					.contains(value)
					|| ForgeRegistries.ITEMS.getKey(stack.getItem())
						.getPath()
						.contains(value)) {
					displayedItemsInCategory.add(entry);
					continue;
				}
			}

			if (displayedItemsInCategory.isEmpty())
				continue;

			if (categoryIndex - extraCategories < currentItemSource.size() - 1)
				anyItemsInCategory = true;
			categoryY += rowHeight;
			categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
		}

		if (!anyItemsInCategory)
			categories.clear();

		clampScrollBar();
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		addressBox.tick();

		boolean allEmpty = true;
		for (List<BigItemStack> list : displayedItems)
			allEmpty &= list.isEmpty();
		if (allEmpty)
			emptyTicks++;
		else
			emptyTicks = 0;

		if (successTicks > 0 && itemsToOrder.isEmpty())
			successTicks++;
		else
			successTicks = 0;

		List<List<BigItemStack>> clientStockSnapshot = blockEntity.getClientStockSnapshot();
		if (clientStockSnapshot != currentItemSource) {
			currentItemSource = clientStockSnapshot;
			refreshSearchResults(false);
			revalidateOrders();
		}

		if (refreshSearchNextTick) {
			refreshSearchNextTick = false;
			refreshSearchResults(true);
		}

		itemScroll.tickChaser();
		if (Math.abs(itemScroll.getValue() - itemScroll.getChaseTarget()) < 1 / 16f)
			itemScroll.setValue(itemScroll.getChaseTarget());

		if (blockEntity.ticksSinceLastUpdate > 15)
			blockEntity.refreshClientStockSnapshot();

		LivingEntity keeper = stockKeeper.get();
		if (keeper == null || !keeper.isAlive())
			removed();
	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(0, 0, -300);
		super.renderBackground(graphics);
		ms.popPose();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		PoseStack ms = graphics.pose();
		float currentScroll = itemScroll.getValue(partialTicks);
		Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);

		int x = getGuiLeft();
		int y = getGuiTop();

		// BG
		HEADER.render(graphics, x - 15, y);
		y += HEADER.getHeight();
		for (int i = 0; i < (windowHeight - HEADER.getHeight() - FOOTER.getHeight()) / BODY.getHeight(); i++) {
			BODY.render(graphics, x - 15, y);
			y += BODY.getHeight();
		}
		FOOTER.render(graphics, x - 15, y);
		y = getGuiTop();

		// Render text input hints
		if (addressBox.getValue()
			.isBlank())
			graphics.drawString(font, addressBox.getMessage(), addressBox.getX(), addressBox.getY(), 0x88dddddd);

		// Render keeper
		int entitySizeOffset = 0;
		LivingEntity keeper = stockKeeper.get();
		if (keeper != null && keeper.isAlive()) {
			ms.pushPose();
			ms.translate(0, 0, -300);
			entitySizeOffset = (int) (Math.max(0, keeper.getBoundingBox()
				.getXsize() - 1) * 50);
			int entityX = x - 35 - entitySizeOffset;
			int entityY = y + windowHeight - 17;
			InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, entityX, entityY, 50, entityX - mouseX,
				Mth.clamp(entityY - mouseY, -50, 10), keeper);
			ms.popPose();
		}

		BlazeBurnerBlockEntity keeperBE = blaze.get();
		if (keeperBE != null && !keeperBE.isRemoved()) {
			ms.pushPose();
			int entityX = x - 35;
			int entityY = y + windowHeight - 23;
			ms.translate(entityX, entityY, -100);
			ms.mulPose(Axis.XP.rotationDegrees(-22.5f));
			ms.mulPose(Axis.YP.rotationDegrees(-45));
			ms.scale(48, -48, 48);
			float animation = keeperBE.headAnimation.getValue(AnimationTickHolder.getPartialTicks()) * .175f;
			float horizontalAngle = AngleHelper.rad(270);
			HeatLevel heatLevel = keeperBE.getHeatLevelForRender();
			boolean canDrawFlame = heatLevel.isAtLeast(HeatLevel.FADING);
			boolean drawGoggles = keeperBE.goggles;
			PartialModel drawHat = AllPartialModels.LOGISTICS_HAT;
			int hashCode = keeperBE.hashCode();
			Lighting.setupForEntityInInventory();
			BlazeBurnerRenderer.renderShared(ms, null, graphics.bufferSource(), minecraft.level,
				keeperBE.getBlockState(), heatLevel, animation, horizontalAngle, canDrawFlame, drawGoggles, drawHat,
				hashCode);
			Lighting.setupFor3DItems();
			ms.popPose();
		}

		// Render static item icons
		if (encodeRequester) {
			ms.pushPose();
			ms.translate(x + windowWidth + 5, y + windowHeight - 70, 0);
			ms.scale(3.5f, 3.5f, 3.5f);
			GuiGameElement.of(itemToProgram)
				.render(graphics);
			ms.popPose();
		}

		// Render ordered items
		for (int index = 0; index < cols; index++) {
			if (itemsToOrder.size() <= index)
				break;

			BigItemStack entry = itemsToOrder.get(index);
			boolean isStackHovered = index == hoveredSlot.getSecond() && hoveredSlot.getFirst() == -1;

			ms.pushPose();
			ms.translate(itemsX + index * colWidth, orderY, 0);
			renderItemEntry(graphics, 1, entry, isStackHovered, true);
			ms.popPose();
		}

		boolean justSent = itemsToOrder.isEmpty() && successTicks > 0;
		if (isConfirmHovered(mouseX, mouseY) && !justSent)
			AllGuiTextures.STOCK_KEEPER_REQUEST_SEND_HOVER.render(graphics, x + windowWidth - 81,
				y + windowHeight - 41);

		MutableComponent headerTitle = CreateLang.translate("gui.stock_keeper.title")
			.component();
		graphics.drawString(font, headerTitle, x + windowWidth / 2 - font.width(headerTitle) / 2, y + 4, 0x714A40,
			false);
		MutableComponent component =
			CreateLang.translate(encodeRequester ? "gui.stock_keeper.configure" : "gui.stock_keeper.send")
				.component();

		if (justSent) {
			float alpha = Mth.clamp((successTicks + partialTicks - 5f) / 5f, 0f, 1f);
			ms.pushPose();
			ms.translate(alpha * alpha * 50, 0, 0);
			if (successTicks < 10)
				graphics.drawString(font, component, x + windowWidth - 42 - font.width(component) / 2,
					y + windowHeight - 35, new Color(0x252525).setAlpha(1 - alpha * alpha)
						.getRGB(),
					false);
			ms.popPose();

		} else {
			graphics.drawString(font, component, x + windowWidth - 42 - font.width(component) / 2,
				y + windowHeight - 35, 0x252525, false);
		}

		// Request just sent
		if (justSent) {
			Component msg = CreateLang.translateDirect("gui.stock_keeper.request_sent");
			float alpha = Mth.clamp((successTicks + partialTicks - 10f) / 5f, 0f, 1f);
			int msgX = x + windowWidth / 2 - font.width(msg) / 2;
			int msgY = orderY + 5;
			if (alpha > 0) {
				int c1 = new Color(0xB59370).setAlpha(alpha)
					.getRGB();
				int c2 = new Color(0xEDD8BB).setAlpha(alpha * 0)
					.getRGB();
				int c3 = new Color(0x714A40).setAlpha(alpha)
					.getRGB();
				TooltipRenderUtil.renderTooltipBackground(graphics, msgX - 2, msgY + 2, font.width(msg) + 4,
					font.lineHeight - 5, 0, c1, c1, c2, c2);
				graphics.drawString(font, msg, msgX, msgY, c3, false);
			}
		}

		int itemWindowX = x + 21;
		int itemWindowX2 = itemWindowX + 184;
		int itemWindowY = y + 17;
		int itemWindowY2 = y + windowHeight - 80;

		UIRenderHelper.swapAndBlitColor(minecraft.getMainRenderTarget(), UIRenderHelper.framebuffer);
		startStencil(graphics, itemWindowX - 5, itemWindowY, itemWindowX2 - itemWindowX + 10,
			itemWindowY2 - itemWindowY);

		ms.pushPose();
		ms.translate(0, -currentScroll * rowHeight, 0);

		// BG
		for (int sliceY = -2; sliceY < getMaxScroll() * rowHeight + windowHeight - 72; sliceY +=
			AllGuiTextures.STOCK_KEEPER_REQUEST_BG.getHeight()) {
			if (sliceY - currentScroll * rowHeight < -20)
				continue;
			if (sliceY - currentScroll * rowHeight > windowHeight - 72)
				continue;
			AllGuiTextures.STOCK_KEEPER_REQUEST_BG.render(graphics, x + 22, y + sliceY + 18);
		}

		// Search bar
		AllGuiTextures.STOCK_KEEPER_REQUEST_SEARCH.render(graphics, x + 42, searchBox.getY() - 5);
		searchBox.render(graphics, mouseX, mouseY, partialTicks);
		if (searchBox.getValue()
			.isBlank() && !searchBox.isFocused())
			graphics.drawString(font, searchBox.getMessage(),
				x + windowWidth / 2 - font.width(searchBox.getMessage()) / 2, searchBox.getY(), 0xff4A2D31, false);

		// Something isnt right
		boolean allEmpty = true;
		for (List<BigItemStack> list : displayedItems)
			allEmpty &= list.isEmpty();
		if (allEmpty) {
			Component msg = getTroubleshootingMessage();
			float alpha = Mth.clamp((emptyTicks - 10f) / 5f, 0f, 1f);
			if (alpha > 0) {
				graphics.drawString(font, msg, x + windowWidth / 2 - font.width(msg) / 2 + 1, itemsY + 20 + 1,
					new Color(0x4A2D31).setAlpha(alpha)
						.getRGB(),
					false);
				graphics.drawString(font, msg, x + windowWidth / 2 - font.width(msg) / 2, itemsY + 20,
					new Color(0xF8F8EC).setAlpha(alpha)
						.getRGB(),
					false);
			}
		}

		// Items
		for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
			List<BigItemStack> category = displayedItems.get(categoryIndex);
			int categoryY = categories.isEmpty() ? 0
				: categories.get(categoryIndex)
					.getSecond();
			if (category.isEmpty())
				continue;

			if (!categories.isEmpty()) {
				graphics.drawString(font, categories.get(categoryIndex)
					.getFirst(), itemsX + 5, itemsY + categoryY + 8, 0x4A2D31, false);
				graphics.drawString(font, categories.get(categoryIndex)
					.getFirst(), itemsX + 4, itemsY + categoryY + 7, 0xF8F8EC, false);
			}

			for (int index = 0; index < category.size(); index++) {
				int pY = itemsY + categoryY + (categories.isEmpty() ? 4 : rowHeight) + (index / cols) * rowHeight;
				float cullY = pY - currentScroll * rowHeight;

				if (cullY < y)
					continue;
				if (cullY > y + windowHeight - 72)
					break;

				boolean isStackHovered = index == hoveredSlot.getSecond() && categoryIndex == hoveredSlot.getFirst();
				BigItemStack entry = category.get(index);

				ms.pushPose();
				ms.translate(itemsX + (index % cols) * colWidth, pY, 0);
				renderItemEntry(graphics, 1, entry, isStackHovered, false);
				ms.popPose();
			}
		}

		// Render lock option
		if (isAdmin)
			(isLocked ? AllGuiTextures.STOCK_KEEPER_REQUEST_LOCKED : AllGuiTextures.STOCK_KEEPER_REQUEST_UNLOCKED)
				.render(graphics, lockX, lockY);

		ms.popPose();
		endStencil();

		// Scroll bar
		int windowH = windowHeight - 92;
		int totalH = getMaxScroll() * rowHeight + windowH;
		int barSize = Math.max(5, Mth.floor((float) windowH / totalH * (windowH - 2)));
		if (barSize < windowH - 2) {
			int barX = itemsX + cols * colWidth;
			int barY = y + 15;
			ms.pushPose();
			ms.translate(0, (currentScroll * rowHeight) / totalH * (windowH - 2), 0);
			AllGuiTextures pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
			graphics.blit(pad.location, barX, barY, pad.getWidth(), barSize, pad.getStartX(), pad.getStartY(),
				pad.getWidth(), pad.getHeight(), 256, 256);
			AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(graphics, barX, barY);
			if (barSize > 16)
				AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(graphics, barX, barY + barSize / 2 - 4);
			AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(graphics, barX, barY + barSize - 5);
			ms.popPose();
		}

		// Render tooltip of hovered item
		if (hoveredSlot != noneHovered) {
			BigItemStack entry = hoveredSlot.getFirst() == -1 ? itemsToOrder.get(hoveredSlot.getSecond())
				: displayedItems.get(hoveredSlot.getFirst())
					.get(hoveredSlot.getSecond());
			if (entry instanceof CraftableBigItemStack cbis) {
				// TODO: what is being used to craft this?
			} else
				graphics.renderTooltip(font, entry.stack, mouseX, mouseY);
		}

		// Render tooltip of lock option
		if (currentScroll < 1 && isAdmin && mouseX > lockX && mouseX <= lockX + 15 && mouseY > lockY
			&& mouseY <= lockY + 15) {
			graphics.renderComponentTooltip(font,
				List.of(
					CreateLang.translate(isLocked ? "gui.stock_keeper.network_locked" : "gui.stock_keeper.network_open")
						.component(),
					CreateLang.translate("gui.stock_keeper.network_lock_tip")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.stock_keeper.network_lock_tip_1")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.stock_keeper.network_lock_tip_2")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component()),
				mouseX, mouseY);
		}

		UIRenderHelper.swapAndBlitColor(UIRenderHelper.framebuffer, minecraft.getMainRenderTarget());
	}

	private void renderItemEntry(GuiGraphics graphics, float scale, BigItemStack entry, boolean isStackHovered,
		boolean isRenderingOrders) {

		int customCount = entry.count;
		if (!isRenderingOrders) {
			BigItemStack order = getOrderForItem(entry);
			if (order != null && entry.count < BigItemStack.INF)
				customCount -= order.count;
			AllGuiTextures.STOCK_KEEPER_REQUEST_SLOT.render(graphics, 0, 0);
		}

		if (entry instanceof CraftableBigItemStack)
			(isRenderingOrders ? AllGuiTextures.STOCK_KEEPER_REQUEST_ORDERED_CRAFTING_SLOT
				: AllGuiTextures.STOCK_KEEPER_REQUEST_CRAFTING_SLOT).render(graphics, 0, 0);

		PoseStack ms = graphics.pose();
		ms.pushPose();

		float scaleFromHover = 1;
		if (isStackHovered)
			scaleFromHover += .075f;

		ms.translate((colWidth - 18) / 2.0, (rowHeight - 18) / 2.0, 0);
		ms.translate(18 / 2.0, 18 / 2.0, 0);
		ms.scale(scale, scale, scale);
		ms.scale(scaleFromHover, scaleFromHover, scaleFromHover);
		ms.translate(-18 / 2.0, -18 / 2.0, 0);
		if (customCount != 0)
			GuiGameElement.of(entry.stack)
				.render(graphics);
		ms.popPose();

		ms.pushPose();
		ms.translate(0, 0, 190);
		if (customCount != 0)
			graphics.renderItemDecorations(font, entry.stack, 1, 1, "");
		ms.translate(0, 0, 10);
		drawItemCount(graphics, entry.count, customCount);
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
					: count >= 100 ? count + "" : count > 0 ? " " + count : "";// " \u2714";

		if (count >= BigItemStack.INF)
			text = "+";

		if (text.isBlank())
			return;

//		int lightOutline = 0x444444;
//		int darkOutline = 0x222222;
//		int middleColor = special ? 0xaaffaa : 0xdddddd;
//		if (" \u2714".equals(text)) {
//			for (int xi : Iterate.positiveAndNegative)
//				graphics.drawString(font, CreateLang.text(text)
//					.component(), 11 + xi, 10, xi < 0 ? lightOutline : darkOutline, false);
//			for (int yi : Iterate.positiveAndNegative)
//				graphics.drawString(font, CreateLang.text(text)
//					.component(), 11, 10 + yi, yi < 0 ? lightOutline : darkOutline, false);
//			graphics.drawString(font, CreateLang.text(text)
//				.component(), 11, 10, middleColor, false);
//			return;
//		}

		int x = (int) Math.floor(-text.length() * 2.5);
		for (char c : text.toCharArray()) {
			int index = c - '0';
			int xOffset = index * 6;
			int spriteWidth = NUMBERS.getWidth();

			switch (c) {
			case ' ':
				x += 4;
				continue;
			case '.':
				spriteWidth = 3;
				xOffset = 60;
				break;
			case 'k':
				xOffset = 64;
				break;
			case 'm':
				spriteWidth = 7;
				xOffset = 70;
				break;
			case '+':
				spriteWidth = 9;
				xOffset = 84;
				break;
			}

			RenderSystem.enableBlend();
			graphics.blit(NUMBERS.location, 14 + x, 10, 0, NUMBERS.getStartX() + xOffset, NUMBERS.getStartY(),
				spriteWidth, NUMBERS.getHeight(), 256, 256);
			x += spriteWidth - 1;
		}

	}

	@Nullable
	private BigItemStack getOrderForItem(BigItemStack stack) {
		for (BigItemStack entry : itemsToOrder) {
			if (entry instanceof CraftableBigItemStack != stack instanceof CraftableBigItemStack)
				continue;
			if (entry instanceof CraftableBigItemStack c1 && stack instanceof CraftableBigItemStack c2
				&& c1.recipe != c2.recipe)
				continue;
			if (ItemHandlerHelper.canItemStacksStack(stack.stack, entry.stack))
				return entry;
		}
		return null;
	}

	private void revalidateOrders() {
		Set<BigItemStack> invalid = new HashSet<>(itemsToOrder);
		InventorySummary summary = blockEntity.lastClientsideStockSnapshotAsSummary;
		if (currentItemSource == null || summary == null) {
			itemsToOrder.removeAll(invalid);
			return;
		}
		for (BigItemStack entry : itemsToOrder) {
			if (entry instanceof CraftableBigItemStack cbis) {
				for (BigItemStack entry2 : recipesToOrder)
					if (entry2 instanceof CraftableBigItemStack cbis2 && cbis.recipe == cbis2.recipe)
						cbis.count = Math.min(cbis2.count, cbis.count);
				invalid.remove(cbis);
				continue;
			}
			entry.count = Math.min(summary.getCountOf(entry.stack), entry.count);
			if (entry.count > 0)
				invalid.remove(entry);
		}

		itemsToOrder.removeAll(invalid);
		if (invalid.stream()
			.anyMatch(i -> i instanceof CraftableBigItemStack)) {
			refreshSearchNextTick = true;
		}
	}

	private Couple<Integer> getHoveredSlot(int x, int y) {
		if (x < itemsX || x >= itemsX + cols * colWidth)
			return noneHovered;

		// Ordered item is hovered
		if (y >= orderY && y < orderY + rowHeight) {
			int col = (x - itemsX) / colWidth;
			if (itemsToOrder.size() <= col || col < 0)
				return noneHovered;
			return Couple.create(-1, col);
		}

		if (y < getGuiTop() + 16 || y > getGuiTop() + windowHeight - 80)
			return noneHovered;
		if (!itemScroll.settled())
			return noneHovered;

		int localY = y - itemsY;

		for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
			Pair<String, Integer> entry = categories.isEmpty() ? Pair.of("", 0) : categories.get(categoryIndex);

			int row =
				Mth.floor((localY - (categories.isEmpty() ? 4 : rowHeight) - entry.getSecond()) / (float) rowHeight)
					+ (int) itemScroll.getChaseTarget();

			int col = (x - itemsX) / colWidth;
			int slot = row * cols + col;

			if (slot < 0)
				return noneHovered;
			if (displayedItems.get(categoryIndex)
				.size() <= slot)
				continue;

			return Couple.create(categoryIndex, slot);
		}

		return noneHovered;
	}

	private boolean isConfirmHovered(int mouseX, int mouseY) {
		int confirmX = getGuiLeft() + 161;
		int confirmY = getGuiTop() + windowHeight - 39;
		int confirmW = 78;
		int confirmH = 18;

		if (mouseX < confirmX || mouseX >= confirmX + confirmW)
			return false;
		if (mouseY < confirmY || mouseY >= confirmY + confirmH)
			return false;
		return true;
	}

	private Component getTroubleshootingMessage() {
		if (currentItemSource == null)
			return CreateLang.translate("gui.stock_keeper.checking_stocks")
				.component();
		if (blockEntity.activeLinks == 0)
			return CreateLang.translate("gui.stock_keeper.no_packagers_linked")
				.component();
		if (currentItemSource.isEmpty())
			return CreateLang.translate("gui.stock_keeper.inventories_empty")
				.component();
		return CreateLang.translate("gui.stock_keeper.no_search_results")
			.component();
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (addressBox.isFocused()) {
			if (addressBox.isHovered())
				return addressBox.mouseClicked(pMouseX, pMouseY, pButton);
			addressBox.setFocused(false);
		}
		if (searchBox.isFocused()) {
			if (searchBox.isHovered())
				return searchBox.mouseClicked(pMouseX, pMouseY, pButton);
			searchBox.setFocused(false);
		}

		Couple<Integer> hoveredSlot = getHoveredSlot((int) pMouseX, (int) pMouseY);

		boolean lmb = pButton == 0;
		boolean rmb = pButton == 1;

		if (isAdmin && itemScroll.getChaseTarget() == 0 && lmb && pMouseX > lockX && pMouseX <= lockX + 15
			&& pMouseY > lockY && pMouseY <= lockY + 15) {
			isLocked = !isLocked;
			AllPackets.getChannel()
				.sendToServer(new StockKeeperLockPacket(blockEntity.getBlockPos(), isLocked));
			return true;
		}

		if (rmb && searchBox.isMouseOver(pMouseX, pMouseY)) {
			searchBox.setValue("");
			refreshSearchNextTick = true;
			searchBox.setFocused(true);
			return true;
		}

		if (lmb && isConfirmHovered((int) pMouseX, (int) pMouseY)) {
			sendIt();
			return true;
		}

		if (hoveredSlot == noneHovered || !lmb && !rmb)
			return super.mouseClicked(pMouseX, pMouseY, pButton);

		BigItemStack entry = hoveredSlot.getFirst() == -1 ? itemsToOrder.get(hoveredSlot.getSecond())
			: displayedItems.get(hoveredSlot.getFirst())
				.get(hoveredSlot.getSecond());

		boolean orderClicked = hoveredSlot.getFirst() == -1;
		ItemStack itemStack = entry.stack;
		BigItemStack existingOrder = getOrderForItem(entry);

		if (existingOrder == null) {
			if (itemsToOrder.size() >= cols || rmb)
				return true;
			if (entry instanceof CraftableBigItemStack cbis) {
				itemsToOrder.add(existingOrder = new CraftableBigItemStack(itemStack.copyWithCount(1), cbis.recipe));
				existingOrder.count = 0;
			} else
				itemsToOrder.add(existingOrder = new BigItemStack(itemStack.copyWithCount(1), 0));
		}

		boolean isCrafted = existingOrder instanceof CraftableBigItemStack;
		int transfer = hasShiftDown() ? itemStack.getMaxStackSize() : hasControlDown() ? 10 : 1;

		if (existingOrder instanceof CraftableBigItemStack cbis) {
			int increments = cbis.recipe.getResultItem(blockEntity.getLevel()
				.registryAccess())
				.getCount();
			transfer = Mth.ceil(transfer / (float) increments) * increments;
		}

		int current = existingOrder.count;

		if (rmb || orderClicked) {
			existingOrder.count = current - transfer;
			if (existingOrder.count <= 0)
				itemsToOrder.remove(existingOrder);
			if (isCrafted)
				refreshSearchResults(false);
			return true;
		}

		existingOrder.count = current + Math.min(transfer, entry.count - current);
		if (isCrafted)
			refreshSearchResults(false);
		return true;
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		return super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		Couple<Integer> hoveredOrderSlot = getHoveredSlot((int) mouseX, (int) mouseY);
		if (hoveredOrderSlot == noneHovered || hoveredOrderSlot.getFirst() != -1) {
			int maxScroll = getMaxScroll();
			int direction = (int) (Math.ceil(Math.abs(delta)) * -Math.signum(delta));
			float newTarget = Mth.clamp(itemScroll.getChaseTarget() + direction, 0, maxScroll);
			itemScroll.chase(newTarget, 0.5, Chaser.EXP);
		}
		return true;
	}

	private void clampScrollBar() {
		int maxScroll = getMaxScroll();
		float prevTarget = itemScroll.getChaseTarget();
		float newTarget = Mth.clamp(prevTarget, 0, maxScroll);
		if (prevTarget != newTarget)
			itemScroll.startWithValue(newTarget);
	}

	private int getMaxScroll() {
		int visibleHeight = windowHeight - 84;
		int totalRows = 0;
		for (List<BigItemStack> list : displayedItems) {
			if (list.isEmpty())
				continue;
			totalRows++;
			totalRows += Math.ceil(list.size() / (float) cols);
		}
		int maxScroll = (int) Math.max(0, (totalRows * rowHeight - visibleHeight + 50) / rowHeight);
		return maxScroll;
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		if (addressBox.isFocused() && addressBox.charTyped(pCodePoint, pModifiers))
			return true;
		String s = searchBox.getValue();
		if (!searchBox.charTyped(pCodePoint, pModifiers))
			return false;
		if (!Objects.equals(s, searchBox.getValue()))
			refreshSearchNextTick = true;
		return true;
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == GLFW.GLFW_KEY_ENTER && searchBox.isFocused()) {
			searchBox.setFocused(false);
			return true;
		}

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
			refreshSearchNextTick = true;
		return true;
	}

	@Override
	public void removed() {
		AllPackets.getChannel()
			.sendToServer(new PackageOrderRequestPacket(blockEntity.getBlockPos(),
				new PackageOrder(Collections.emptyList()), addressBox.getValue(), false));
		super.removed();
	}

	private void sendIt() {
		revalidateOrders();
		if (itemsToOrder.isEmpty())
			return;

		// TODO convert craftable to their respective ingredients

		AllPackets.getChannel()
			.sendToServer(new PackageOrderRequestPacket(blockEntity.getBlockPos(), new PackageOrder(itemsToOrder),
				addressBox.getValue(), encodeRequester));

		itemsToOrder = new ArrayList<>();
		recipesToOrder = new ArrayList<>();
		blockEntity.ticksSinceLastUpdate = 10;
		successTicks = 1;

		if (encodeRequester)
			minecraft.setScreen(null);
	}

	@Override
	public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
		return super.keyReleased(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	private boolean hasCraftables() {
		return !recipesToOrder.isEmpty();
	}

	private ArrayList<BigItemStack> getCraftables() {
		ArrayList<BigItemStack> list = new ArrayList<>();
		for (BigItemStack ordered : recipesToOrder)
			if (ordered instanceof CraftableBigItemStack cbis)
				list.add(new CraftableBigItemStack(cbis.stack, cbis.recipe));
		updateCraftableAmounts(list);
		return list;
	}

	private void updateCraftableAmounts(List<BigItemStack> craftables) {
		InventorySummary usedItems = new InventorySummary();
		InventorySummary availableItems = blockEntity.lastClientsideStockSnapshotAsSummary;
		if (availableItems == null)
			return;

		List<BigItemStack> craftablesOrderedFirst = new ArrayList<>();
		for (BigItemStack bis : itemsToOrder)
			if (bis instanceof CraftableBigItemStack cbis)
				for (BigItemStack bis2 : craftables)
					if (bis2 instanceof CraftableBigItemStack cbis2 && cbis.recipe == cbis2.recipe)
						craftablesOrderedFirst.add(cbis2);

		for (BigItemStack bis : craftables)
			if (!craftablesOrderedFirst.contains(bis))
				craftablesOrderedFirst.add(bis);

		Orders: for (BigItemStack ordered : craftablesOrderedFirst) {
			if (!(ordered instanceof CraftableBigItemStack cbis))
				continue;

			List<Ingredient> ingredients = cbis.getIngredients();
			List<List<BigItemStack>> validEntriesByIngredient = new ArrayList<>();
			List<ItemStack> visited = new ArrayList<>();

			for (Ingredient ingredient : ingredients) {
				if (ingredient.isEmpty())
					continue;
				List<BigItemStack> valid = new ArrayList<>();
				for (List<BigItemStack> list : availableItems.getItemMap()
					.values())
					Entries: for (BigItemStack entry : list) {
						if (!ingredient.test(entry.stack))
							continue;
						BigItemStack asBis = new BigItemStack(entry.stack, entry.count);
						BigItemStack orderForItem = getOrderForItem(asBis);
						if (orderForItem != null)
							asBis.count -= orderForItem.count;
						asBis.count = Math.max(0, asBis.count - usedItems.getCountOf(entry.stack));
						valid.add(asBis);
						for (ItemStack visitedStack : visited) {
							if (!ItemHandlerHelper.canItemStacksStack(visitedStack, entry.stack))
								continue;
							visitedStack.grow(1);
							continue Entries;
						}
						visited.add(entry.stack.copyWithCount(1));
					}

				if (valid.isEmpty()) {
					cbis.count = 0;
					continue Orders;
				}

				validEntriesByIngredient.add(valid);
			}

			// Ingredients with shared items must divide counts
			for (ItemStack visitedItem : visited) {
				for (List<BigItemStack> list : validEntriesByIngredient) {
					for (BigItemStack entry : list) {
						if (!ItemHandlerHelper.canItemStacksStack(entry.stack, visitedItem))
							continue;
						entry.count = entry.count / visitedItem.getCount();
					}
				}
			}

			// Determine the bottlenecking ingredient
			int minCount = Integer.MAX_VALUE;
			for (List<BigItemStack> list : validEntriesByIngredient) {
				int sum = 0;
				for (BigItemStack entry : list)
					sum += entry.count;
				minCount = Math.min(sum, minCount);
			}

			int outputCount = cbis.recipe.getResultItem(blockEntity.getLevel()
				.registryAccess())
				.getCount();

			cbis.count = minCount * outputCount;

			// Use ingredients up before checking next recipe
			BigItemStack orderForItem = getOrderForItem(cbis);
			if (orderForItem == null)
				continue Orders;
			for (List<BigItemStack> list : validEntriesByIngredient) {
				int remaining = orderForItem.count / outputCount;
				for (BigItemStack entry : list) {
					if (remaining <= 0)
						break;
					usedItems.add(entry.stack, Math.min(remaining, entry.count));
					remaining -= entry.count;
				}
			}

			continue Orders;
		}

	}

}
