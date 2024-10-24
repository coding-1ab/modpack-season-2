package com.simibubi.create.content.logistics.factoryBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
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
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class FactoryPanelScreen extends AbstractSimiScreen {

	private EditBox addressBox;
	private IconButton confirmButton;
	private IconButton newInputButton;
	private ScrollInput promiseExpiration;
	private FactoryPanelBehaviour behaviour;
	private int displayedExtraRows;

	private BigItemStack outputConfig;
	private List<BigItemStack> inputConfig;
	private List<FactoryPanelConnection> connections;

	public FactoryPanelScreen(FactoryPanelBehaviour behaviour) {
		this.behaviour = behaviour;
		minecraft = Minecraft.getInstance();
		updateConfigs();
	}

	private void updateConfigs() {
		connections = new ArrayList<>(behaviour.targetedBy.values());
		outputConfig = new BigItemStack(behaviour.getFilter(), behaviour.recipeOutput);
		inputConfig = connections.stream()
			.map(c -> {
				FactoryPanelBehaviour b = FactoryPanelBehaviour.at(minecraft.level, c.from());
				return b == null ? new BigItemStack(ItemStack.EMPTY, 0) : new BigItemStack(b.getFilter(), c.amount());
			})
			.toList();
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
			addressBox =
				new AddressEditBox(this, new NoShadowFontWrapper(font), x + 38, y + 30 + middleHeight(), 110, 10);
			addressBox.setValue(behaviour.recipeAddress);
			addressBox.setTextColor(0x555555);
		}
		addressBox.setY(y + 30 + middleHeight());
		addRenderableWidget(addressBox);

		confirmButton = new IconButton(x + sizeX - 51, y + sizeY - 22, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.setScreen(null));
		addRenderableWidget(confirmButton);

		promiseExpiration = new ScrollInput(x + 112, y + 54 + middleHeight(), 25, 16).withRange(-1, 31)
			.titled(CreateLang.temporaryText("Promises expire after")
				.component());
		promiseExpiration.setState(behaviour.promiseClearingInterval);
		addRenderableWidget(promiseExpiration);

		if (behaviour.targetedBy.size() < 9) {
			int slot = behaviour.targetedBy.size();
			newInputButton = new IconButton(x + 24 + (slot % 3 * 18), y + 27 + (slot / 3 * 18), AllIcons.I_ADD);
			newInputButton.withCallback(() -> {
				FactoryPanelConnectionHandler.startConnection(behaviour);
				minecraft.setScreen(null);
			});
			newInputButton.setToolTip(CreateLang.temporaryText("Connect an input panel")
				.component());
			addRenderableWidget(newInputButton);
		}

		displayedExtraRows = Math.min((behaviour.targetedBy.size() / 3), 2);
	}

	private int middleHeight() {
		return AllGuiTextures.FACTORY_PANEL_MIDDLE.getHeight() + Math.min((behaviour.targetedBy.size() / 3), 2) * 18;
	}

	@Override
	public void tick() {
		super.tick();
		if (inputConfig.size() != behaviour.targetedBy.size()
			|| displayedExtraRows != Math.min((behaviour.targetedBy.size() / 3), 2)) {
			updateConfigs();
			init();
		}
		addressBox.tick();
		promiseExpiration.titled(CreateLang
			.temporaryText(promiseExpiration.getState() == -1 ? "Promises do not expire" : "Promises expire after:")
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
		for (int frame : Iterate.zeroAndOne) {
			AllGuiTextures sprite =
				frame == 0 ? AllGuiTextures.FACTORY_PANEL_SLOT_FRAME : AllGuiTextures.FACTORY_PANEL_SLOT;
			for (slot = 0; slot < behaviour.targetedBy.size(); slot++)
				sprite.render(graphics, x + 23 + frame + (slot % 3 * 18), y + 26 + frame + (slot / 3 * 18));
			if (slot < 9)
				sprite.render(graphics, x + 23 + frame + (slot % 3 * 18), y + 26 + frame + (slot / 3 * 18));
		}

		slot = 0;
		for (BigItemStack itemStack : inputConfig) {
			int inputX = x + 25 + (slot % 3 * 18);
			int inputY = y + 28 + (slot / 3 * 18);
			graphics.renderItem(itemStack.stack, inputX, inputY);
			if (!itemStack.stack.isEmpty())
				graphics.renderItemDecorations(font, itemStack.stack, inputX, inputY, itemStack.count + "");

			slot++;
			
			if (mouseX >= inputX - 1 && mouseX < inputX - 1 + 18 && mouseY >= inputY - 1 && mouseY < inputY - 1 + 18) {
				if (itemStack.stack.isEmpty()) {
					graphics.renderComponentTooltip(font, List.of(CreateLang.temporaryText("Empty panel")
						.color(ScrollInput.HEADER_RGB)
						.component(),
						CreateLang.temporaryText("Left-Click to disconnect")
							.style(ChatFormatting.DARK_GRAY)
							.style(ChatFormatting.ITALIC)
							.component()),
						mouseX, mouseY);
				} else
					graphics.renderComponentTooltip(font, List.of(CreateLang.temporaryText("Send ")
						.add(CreateLang.itemName(itemStack.stack)
							.add(CreateLang.text(" x" + itemStack.count)))
						.color(ScrollInput.HEADER_RGB)
						.component(),
						CreateLang.temporaryText("Scroll to change amount")
							.style(ChatFormatting.DARK_GRAY)
							.style(ChatFormatting.ITALIC)
							.component(),
						CreateLang.temporaryText("Left-Click to disconnect")
							.style(ChatFormatting.DARK_GRAY)
							.style(ChatFormatting.ITALIC)
							.component()),
						mouseX, mouseY);
			}
		}

		if (inputConfig.size() > 0) {
			AllGuiTextures.FACTORY_PANEL_ARROW.render(graphics,
				x + 75 + Mth.clamp(behaviour.targetedBy.size(), 0, 2) * 9, y + 16 + middleHeight() / 2);
			int outputX = x + 130;
			int outputY = y + 16 + middleHeight() / 2;
			AllGuiTextures.FACTORY_PANEL_SLOT_FRAME.render(graphics, outputX - 2, outputY - 2);
			graphics.renderItem(outputConfig.stack, outputX, outputY);
			graphics.renderItemDecorations(font, behaviour.getFilter(), outputX, outputY, outputConfig.count + "");

			if (mouseX >= outputX - 1 && mouseX < outputX - 1 + 18 && mouseY >= outputY - 1
				&& mouseY < outputY - 1 + 18) {
				graphics.renderComponentTooltip(font, List.of(CreateLang.temporaryText("Expect ")
					.add(CreateLang.itemName(outputConfig.stack)
						.add(CreateLang.text(" x" + outputConfig.count)))
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.temporaryText("Scroll to change amount")
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
		if (addressBox.isHovered() && !addressBox.isFocused()) {
			if (addressBox.getValue()
				.isBlank())
				graphics.renderComponentTooltip(font, List.of(CreateLang.temporaryText("Send inputs to...")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.temporaryText("Enter an address where")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.temporaryText("this recipe is carried out.")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.schedule.lmb_edit")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component()),
					mouseX, mouseY);
			else
				graphics.renderComponentTooltip(font, List.of(CreateLang.temporaryText("Sending inputs to")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.temporaryText("'" + addressBox.getValue() + "'")
						.style(ChatFormatting.GRAY)
						.component()),
					mouseX, mouseY);
		}

		// TITLE
		Component title = CreateLang.temporaryText("Logistics Recipe")
			.component();
		graphics.drawString(font, title, x + 87 - font.width(title) / 2, y + 7, 0x3D3C48, false);

		// ITEM PREVIEW
		ms.pushPose();
		ms.translate(0, middleHeight() - 25, 0);
		GuiGameElement.of(AllBlocks.FACTORY_PANEL.asStack())
			.scale(4)
			.at(0, 0, -200)
			.render(graphics, x + 175, y + 55);
		if (!behaviour.getFilter()
			.isEmpty()) {
			GuiGameElement.of(behaviour.getFilter())
				.scale(1.625)
				.at(0, 0, 200)
				.render(graphics, x + 194, y + 68);
		}
		ms.popPose();

		// PROMISES
		int state = promiseExpiration.getState();
		graphics.drawString(font, CreateLang.temporaryText(state == -1 ? " /" : state == 0 ? "30s" : state + "m")
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
				promiseTip = List.of(CreateLang.temporaryText("No open promises")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.temporaryText("When inputs are sent, a promise")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.temporaryText("is held until outputs arrive.")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.temporaryText("This prevents over-sending.")
						.style(ChatFormatting.GRAY)
						.component());
			} else {
				promiseTip = List.of(CreateLang.temporaryText("Promised Items")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.temporaryText(behaviour.getFilter()
						.getHoverName()
						.getString() + " x" + promised)
						.component(),
					CreateLang.temporaryText("Left-Click to reset")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component());
			}

			graphics.renderComponentTooltip(font, promiseTip, mouseX, mouseY);
		}

		ms.popPose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
		if (getFocused() != null && !getFocused().isMouseOver(mouseX, mouseY))
			setFocused(null);

		int x = guiLeft;
		int y = guiTop;

		// Remove connections
		for (int i = 0; i < connections.size(); i++) {
			int inputX = x + 25 + (i % 3 * 18);
			int inputY = y + 28 + (i / 3 * 18);
			if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
				sendIt(connections.get(i)
					.from(), false);
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
			for (int i = 0; i < inputConfig.size(); i++)
				inputs.put(connections.get(i)
					.from(), inputConfig.get(i).count);
		AllPackets.getChannel()
			.sendToServer(new FactoryPanelConfigurationPacket(behaviour.getPanelPosition(), addressBox.getValue(),
				inputs, outputConfig.count, promiseExpiration.getState(), toRemove, clearPromises));
	}

}
