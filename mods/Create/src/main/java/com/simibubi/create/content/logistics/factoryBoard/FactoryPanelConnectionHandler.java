package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllPackets;

import net.minecraft.world.level.LevelAccessor;

public class FactoryPanelConnectionHandler {

	static FactoryPanelPosition connectingFrom;

	public static void panelClicked(LevelAccessor level, FactoryPanelPosition panelPos) {
		if (connectingFrom == null) {
			connectingFrom = panelPos;
			return;
		}

		AllPackets.getChannel()
			.sendToServer(new FactoryPanelConnectionPacket(connectingFrom, panelPos));
		connectingFrom = null;
	}

}
