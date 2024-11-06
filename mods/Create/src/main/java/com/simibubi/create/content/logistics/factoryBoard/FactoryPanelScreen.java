package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.Iterate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.ItemHandlerHelper;

public class FactoryPanelScreen extends AbstractSimiScreen {

	private EditBox addressBox;
	private IconButton confirmButton;
	private IconButton newInputButton;
	private ScrollInput promiseExpiration;
	private FactoryPanelBehaviour behaviour;
	private int displayedExtraRows;
	private boolean restocker;

	private BigItemStack outputConfig;
	private List<BigItemStack> inputConfig;
	private List<FactoryPanelConnection> connections;

	private CraftingRecipe availableCraftingRecipe;
	private boolean craftingActive;
	private List<BigItemStack> craftingIngredients;

	public FactoryPanelScreen(FactoryPanelBehaviour behaviour) {
		this.behaviour = behaviour;
		minecraft = Minecraft.getInstance();
		restocker = behaviour.panelBE().restocker;
		availableCraftingRecipe = null;
		updateConfigs();
	}

	private void updateConfigs() {
		connections = new ArrayList<>(behaviour.targetedBy.values());
		outputConfig = new BigItemStack(behaviour.getFilter(), behaviour.recipeOutput);
		inputConfig = connections.stream()
			.map(c -> {
				FactoryPanelBehaviour b = FactoryPanelBehaviour.at(minecraft.level, c.from);
				return b == null ? new BigItemStack(ItemStack.EMPTY, 0) : new BigItemStack(b.getFilter(), c.amount);
			})
			.toList();
		
//		searchForCraftingRecipe(); TODO finish crafter integration

		craftingActive = false;
		if (availableCraftingRecipe == null)
			return;

		outputConfig.count = availableCraftingRecipe.getResultItem(minecraft.level.registryAccess())
			.getCount();
		craftingIngredients = new ArrayList<>();
		craftingActive = true;

		Ingredients: for (Ingredient ingredient : availableCraftingRecipe.getIngredients()) {
			if (ingredient.isEmpty()) {
				craftingIngredients.add(new BigItemStack(ItemStack.EMPTY, 1));
				continue;
			}
			for (BigItemStack bigItemStack : inputConfig) {
				if (!ingredient.test(bigItemStack.stack))
					continue;
				craftingIngredients.add(bigItemStack);
				continue Ingredients;
			}
			while (craftingIngredients.size() < 9)
				craftingIngredients.add(new BigItemStack(ItemStack.EMPTY, 1));
		}
	}

	@Override
	protected void init() {
		int sizeX = 200;
		int sizeY = 75 + middleHeight();
		setWindowSize(sizeX, sizeY);
		super.init();
		clearWidgets();
		int x = guiLeft;
		int y = guiTop;

		if (addressBox == null) {
			addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), x + 38, y + 30 + middleHeight(), 110,
				10, false);
			addressBox.setValue(behaviour.recipeAddress);
			addressBox.setTextColor(0x555555);
		}
		addressBox.setX(x + 38);
		addressBox.setY(y + 30 + middleHeight());
		addRenderableWidget(addressBox);

		confirmButton = new IconButton(x + sizeX - 51, y + sizeY - 22, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.setScreen(null));
		addRenderableWidget(confirmButton);

		promiseExpiration = new ScrollInput(x + 112, y + 54 + middleHeight(), 25, 16).withRange(-1, 31)
			.titled(CreateLang.translate("gui.factory_panel.promises_expire_title")
				.component());
		promiseExpiration.setState(behaviour.promiseClearingInterval);
		addRenderableWidget(promiseExpiration);

		if (!craftingActive && !restocker && behaviour.targetedBy.size() < 9) {
			int slot = behaviour.targetedBy.size();
			newInputButton = new IconButton(x + 24 + (slot % 3 * 18), y + 27 + (slot / 3 * 18), AllIcons.I_ADD);
			newInputButton.withCallback(() -> {
				FactoryPanelConnectionHandler.startConnection(behaviour);
				minecraft.setScreen(null);
			});
			newInputButton.setToolTip(CreateLang.translate("gui.factory_panel.connect_input")
				.component());
			addRenderableWidget(newInputButton);
		}

		displayedExtraRows = rowsToDisplay();
	}

	private int rowsToDisplay() {
		return craftingActive ? 2 : Math.min((behaviour.targetedBy.size() / 3), 2);
	}

	private int middleHeight() {
		return AllGuiTextures.FACTORY_PANEL_MIDDLE.getHeight() + rowsToDisplay() * 18;
	}

	@Override
	public void tick() {
		super.tick();
		if (inputConfig.size() != behaviour.targetedBy.size() || displayedExtraRows != rowsToDisplay()) {
			updateConfigs();
			init();
		}
		addressBox.tick();
		promiseExpiration.titled(CreateLang
			.translate(promiseExpiration.getState() == -1 ? "gui.factory_panel.promises_do_not_expire"
				: "gui.factory_panel.promises_expire_title")
			.component());
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		// BG
		AllGuiTextures.FACTORY_PANEL_TOP.render(graphics, x, y);
		y += AllGuiTextures.FACTORY_PANEL_TOP.getHeight();
		for (int i = 0; i < displayedExtraRows + 1; i++)
			AllGuiTextures.FACTORY_PANEL_MIDDLE.render(graphics, x, y + i * 18);
		y += middleHeight();
		AllGuiTextures.FACTORY_PANEL_BOTTOM.render(graphics, x, y);
		y = guiTop;

		// RECIPE
		int slot = 0;
		int slotsToRender = craftingActive ? 9 : behaviour.targetedBy.size();
		for (int frame : Iterate.zeroAndOne) {
			AllGuiTextures sprite =
				frame == 0 ? AllGuiTextures.FACTORY_PANEL_SLOT_FRAME : AllGuiTextures.FACTORY_PANEL_SLOT;
			for (slot = 0; slot < slotsToRender; slot++)
				sprite.render(graphics, x + 23 + frame + (slot % 3 * 18), y + 26 + frame + (slot / 3 * 18));
			if (slot < 9)
				sprite.render(graphics, x + 23 + frame + (slot % 3 * 18), y + 26 + frame + (slot / 3 * 18));
		}

		slot = 0;

		if (craftingActive) {
			for (BigItemStack itemStack : craftingIngredients)
				renderInputItem(graphics, slot++, itemStack, mouseX, mouseY);

		} else
			for (BigItemStack itemStack : inputConfig)
				renderInputItem(graphics, slot++, itemStack, mouseX, mouseY);

		if (restocker)
			renderInputItem(graphics, slot, new BigItemStack(behaviour.getFilter(), 1), mouseX, mouseY);

		if (inputConfig.size() > 0) {
			int arrowOffset = Mth.clamp(slotsToRender, 0, 2);
			AllGuiTextures.FACTORY_PANEL_ARROW.render(graphics, x + 75 + arrowOffset * 9, y + 16 + middleHeight() / 2);
			int outputX = x + 130;
			int outputY = y + 16 + middleHeight() / 2;
			AllGuiTextures.FACTORY_PANEL_SLOT_FRAME.render(graphics, outputX - 2, outputY - 2);
			graphics.renderItem(outputConfig.stack, outputX, outputY);
			graphics.renderItemDecorations(font, behaviour.getFilter(), outputX, outputY, outputConfig.count + "");

			if (mouseX >= outputX - 1 && mouseX < outputX - 1 + 18 && mouseY >= outputY - 1
				&& mouseY < outputY - 1 + 18) {
				graphics.renderComponentTooltip(font,
					List.of(
						CreateLang
							.translate("gui.factory_panel.expected_output", CreateLang.itemName(outputConfig.stack)
								.add(CreateLang.text(" x" + outputConfig.count))
								.string())
							.color(ScrollInput.HEADER_RGB)
							.component(),
						CreateLang.translate("gui.factory_panel.expected_output_tip")
							.style(ChatFormatting.GRAY)
							.component(),
						CreateLang.translate("gui.factory_panel.expected_output_tip_1")
							.style(ChatFormatting.GRAY)
							.component(),
						CreateLang.translate("gui.factory_panel.expected_output_tip_2")
							.style(ChatFormatting.DARK_GRAY)
							.style(ChatFormatting.ITALIC)
							.component()),
					mouseX, mouseY);
			}
		}

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(0, 0, 10);

		// ADDRESS
		if (addressBox.isHovered() && !addressBox.isFocused())
			showAddressBoxTooltip(graphics, mouseX, mouseY);

		// TITLE
		Component title = CreateLang
			.translate(restocker ? "gui.factory_panel.title_as_restocker" : "gui.factory_panel.title_as_recipe")
			.component();
		graphics.drawString(font, title, x + 87 - font.width(title) / 2, y + 7, 0x3D3C48, false);

		// ITEM PREVIEW
		ms.pushPose();
		ms.translate(0, middleHeight() - 25, 0);
		GuiGameElement.of(AllBlocks.FACTORY_GAUGE.asStack())
			.scale(4)
			.at(0, 0, -200)
			.render(graphics, x + 175, y + 55);
		if (!behaviour.getFilter()
			.isEmpty()) {
			GuiGameElement.of(behaviour.getFilter())
				.scale(1.625)
				.at(0, 0, 100)
				.render(graphics, x + 194, y + 68);
		}

		ms.translate(0, 0, 350);
		MutableComponent countLabelForValueBox = behaviour.getCountLabelForValueBox();
		graphics.drawString(font, countLabelForValueBox, x + 210 - font.width(countLabelForValueBox) / 2, y + 98,
			0xffffffff);
		ms.popPose();

		if (mouseX >= x + 190 - 1 && mouseX < x + 190 - 1 + 48 && mouseY >= y + middleHeight() - 25 + 90 - 1
			&& mouseY < y + middleHeight() - 25 + 94 - 1 + 26)
			showStockLevelTooltip(graphics, mouseX, mouseY);

		// PROMISES
		int state = promiseExpiration.getState();
		graphics.drawString(font, CreateLang.text(state == -1 ? " /" : state == 0 ? "30s" : state + "m")
			.component(), promiseExpiration.getX() + 3, promiseExpiration.getY() + 4, 0xffeeeeee, true);

		ItemStack asStack = AllItems.CARDBOARD_PACKAGE_12x12.asStack();
		int itemY = y + 54 + middleHeight();
		int itemX = x + 88;
		graphics.renderItem(asStack, itemX, itemY);
		int promised = behaviour.getPromised();
		graphics.renderItemDecorations(font, asStack, itemX, itemY, promised + "");

		if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
			List<Component> promiseTip = List.of();

			if (promised == 0) {
				promiseTip = List.of(CreateLang.translate("gui.factory_panel.no_open_promises")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang
						.translate(restocker ? "gui.factory_panel.restocker_promises_tip"
							: "gui.factory_panel.recipe_promises_tip")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang
						.translate(restocker ? "gui.factory_panel.restocker_promises_tip_1"
							: "gui.factory_panel.recipe_promises_tip_1")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.factory_panel.promise_prevents_oversending")
						.style(ChatFormatting.GRAY)
						.component());
			} else {
				promiseTip = List.of(CreateLang.translate("gui.factory_panel.promised_items")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.text(behaviour.getFilter()
						.getHoverName()
						.getString() + " x" + promised)
						.component(),
					CreateLang.translate("gui.factory_panel.left_click_reset")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component());
			}

			graphics.renderComponentTooltip(font, promiseTip, mouseX, mouseY);
		}

		ms.popPose();
	}

	//

	private void renderInputItem(GuiGraphics graphics, int slot, BigItemStack itemStack, int mouseX, int mouseY) {
		int inputX = guiLeft + 25 + (slot % 3 * 18);
		int inputY = guiTop + 28 + (slot / 3 * 18);

		graphics.renderItem(itemStack.stack, inputX, inputY);
		if (!craftingActive && !restocker && !itemStack.stack.isEmpty())
			graphics.renderItemDecorations(font, itemStack.stack, inputX, inputY, itemStack.count + "");

		if (mouseX < inputX - 1 || mouseX >= inputX - 1 + 18 || mouseY < inputY - 1 || mouseY >= inputY - 1 + 18)
			return;

		if (craftingActive)
			return;

		if (itemStack.stack.isEmpty()) {
			graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.factory_panel.empty_panel")
				.color(ScrollInput.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.factory_panel.left_click_disconnect")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component()),
				mouseX, mouseY);
			return;
		}

		if (restocker) {
			graphics.renderComponentTooltip(font,
				List.of(CreateLang.translate("gui.factory_panel.sending_item", CreateLang.itemName(itemStack.stack)
					.string())
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.translate("gui.factory_panel.sending_item_tip")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.factory_panel.sending_item_tip_1")
						.style(ChatFormatting.GRAY)
						.component()),
				mouseX, mouseY);
			return;
		}

		graphics.renderComponentTooltip(font,
			List.of(CreateLang.translate("gui.factory_panel.sending_item", CreateLang.itemName(itemStack.stack)
				.add(CreateLang.text(" x" + itemStack.count))
				.string())
				.color(ScrollInput.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.factory_panel.scroll_to_change_amount")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component(),
				CreateLang.translate("gui.factory_panel.left_click_disconnect")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component()),
			mouseX, mouseY);
	}

	private void showStockLevelTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.renderComponentTooltip(font,
			List.of(
				(behaviour.count > 0 ? CreateLang.translate("gui.factory_panel.storage_level_and_target")
					: CreateLang.translate("gui.factory_panel.storage_level")).color(ScrollInput.HEADER_RGB)
						.component(),
				CreateLang.translate("gui.factory_panel.storage_level_tip")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.factory_panel.storage_level_tip_1")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.factory_panel.storage_level_tip_2")
					.style(ChatFormatting.GRAY)
					.component()),
			mouseX, mouseY);
	}

	private void showAddressBoxTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		if (addressBox.getValue()
			.isBlank()) {
			if (restocker) {
				graphics.renderComponentTooltip(font,
					List.of(CreateLang.translate("gui.factory_panel.restocker_address")
						.color(ScrollInput.HEADER_RGB)
						.component(),
						CreateLang.translate("gui.factory_panel.restocker_address_tip")
							.style(ChatFormatting.GRAY)
							.component(),
						CreateLang.translate("gui.factory_panel.restocker_address_tip_1")
							.style(ChatFormatting.GRAY)
							.component(),
						CreateLang.translate("gui.schedule.lmb_edit")
							.style(ChatFormatting.DARK_GRAY)
							.style(ChatFormatting.ITALIC)
							.component()),
					mouseX, mouseY);

			} else {
				graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.factory_panel.recipe_address")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.translate("gui.factory_panel.recipe_address_tip")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.factory_panel.recipe_address_tip_1")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.schedule.lmb_edit")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component()),
					mouseX, mouseY);
			}
		} else
			graphics.renderComponentTooltip(font,
				List.of(
					CreateLang
						.translate(restocker ? "gui.factory_panel.restocker_address_given"
							: "gui.factory_panel.recipe_address_given")
						.color(ScrollInput.HEADER_RGB)
						.component(),
					CreateLang.text("'" + addressBox.getValue() + "'")
						.style(ChatFormatting.GRAY)
						.component()),
				mouseX, mouseY);
	}

	//

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
		if (getFocused() != null && !getFocused().isMouseOver(mouseX, mouseY))
			setFocused(null);

		int x = guiLeft;
		int y = guiTop;

		// Remove connections
		if (!craftingActive)
			for (int i = 0; i < connections.size(); i++) {
				int inputX = x + 25 + (i % 3 * 18);
				int inputY = y + 28 + (i / 3 * 18);
				if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
					sendIt(connections.get(i).from, false);
					return true;
				}
			}

		// Clear promises
		int itemY = y + 54 + middleHeight();
		int itemX = x + 88;
		if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
			sendIt(null, true);
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double pDelta) {
		int x = guiLeft;
		int y = guiTop;

		if (craftingActive)
			return super.mouseScrolled(mouseX, mouseY, pDelta);

		for (int i = 0; i < inputConfig.size(); i++) {
			int inputX = x + 25 + (i % 3 * 18);
			int inputY = y + 28 + (i / 3 * 18);
			if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
				BigItemStack itemStack = inputConfig.get(i);
				if (itemStack.stack.isEmpty())
					return true;
				itemStack.count =
					Mth.clamp((int) (itemStack.count + Math.signum(pDelta) * (hasShiftDown() ? 10 : 1)), 1, 64);
				return true;
			}
		}

		if (inputConfig.size() > 0) {
			int outputX = x + 130;
			int outputY = y + 16 + middleHeight() / 2;
			if (mouseX >= outputX && mouseX < outputX + 16 && mouseY >= outputY && mouseY < outputY + 16) {
				BigItemStack itemStack = outputConfig;
				itemStack.count =
					Mth.clamp((int) (itemStack.count + Math.signum(pDelta) * (hasShiftDown() ? 10 : 1)), 1, 64);
				return true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, pDelta);
	}

	@Override
	public void removed() {
		sendIt(null, false);
		super.removed();
	}

	private void sendIt(@Nullable FactoryPanelPosition toRemove, boolean clearPromises) {
		Map<FactoryPanelPosition, Integer> inputs = new HashMap<>();

		if (inputConfig.size() == connections.size())
			for (int i = 0; i < inputConfig.size(); i++) {
				BigItemStack stackInConfig = inputConfig.get(i);
				inputs.put(connections.get(i).from, craftingActive ? (int) craftingIngredients.stream()
					.filter(
						b -> !b.stack.isEmpty() && ItemHandlerHelper.canItemStacksStack(b.stack, stackInConfig.stack))
					.count() : stackInConfig.count);
			}

		List<ItemStack> craftingArrangement = craftingActive ? craftingIngredients.stream()
			.map(b -> b.stack)
			.toList() : List.of();

		FactoryPanelPosition pos = behaviour.getPanelPosition();
		int promiseExp = promiseExpiration.getState();
		String address = addressBox.getValue();

		FactoryPanelConfigurationPacket packet = new FactoryPanelConfigurationPacket(pos, address, inputs,
			craftingArrangement, outputConfig.count, promiseExp, toRemove, clearPromises);
		AllPackets.getChannel()
			.sendToServer(packet);
	}

	private void searchForCraftingRecipe() {
		ItemStack output = outputConfig.stack;
		if (output.isEmpty())
			return;
		if (behaviour.targetedBy.isEmpty())
			return;

		Set<Item> itemsToUse = inputConfig.stream()
			.map(b -> b.stack)
			.filter(i -> !i.isEmpty())
			.map(i -> i.getItem())
			.collect(Collectors.toSet());

		ClientLevel level = Minecraft.getInstance().level;

		availableCraftingRecipe = level.getRecipeManager()
			.getAllRecipesFor(RecipeType.CRAFTING)
			.parallelStream()
			.filter(r -> r.getSerializer() == RecipeSerializer.SHAPED_RECIPE
				|| r.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE)
			.filter(r -> output.getItem() == r.getResultItem(level.registryAccess())
				.getItem())
			.filter(r -> {
				if (AllRecipeTypes.shouldIgnoreInAutomation(r))
					return false;

				Set<Item> itemsUsed = new HashSet<>();
				for (Ingredient ingredient : r.getIngredients()) {
					if (ingredient.isEmpty())
						continue;
					boolean available = false;
					for (BigItemStack bis : inputConfig) {
						if (!bis.stack.isEmpty() && ingredient.test(bis.stack)) {
							available = true;
							itemsUsed.add(bis.stack.getItem());
							break;
						}
					}
					if (!available)
						return false;
				}

				if (itemsUsed.size() < itemsToUse.size())
					return false;

				return true;
			})
			.findAny()
			.orElse(null);
	}

}
