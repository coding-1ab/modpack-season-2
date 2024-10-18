package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllPackets;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class FactoryPanelAddressInputScreen extends AbstractSimiScreen {

	EditBox addressBox;
	private FactoryPanelConnectionBehaviour behaviour;

	public FactoryPanelAddressInputScreen(FactoryPanelConnectionBehaviour behaviour) {
		this.behaviour = behaviour;
	}

	@Override
	protected void init() {
		int sizeX = 200;
		int sizeY = 20;
		setWindowSize(sizeX, sizeY);
		super.init();
		clearWidgets();
		addressBox = new EditBox(font, guiLeft, guiTop, sizeX, sizeY, Component.empty());
		addressBox.setValue(behaviour.address);
		addRenderableWidget(addressBox);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removed() {
		super.removed();
		AllPackets.getChannel()
			.sendToServer(
				new FactoryPanelConfigurationPacket(behaviour.getPos(), behaviour.side, addressBox.getValue()));
	}

}
