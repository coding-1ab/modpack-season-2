package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllPackets;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class FactoryPanelConnectionHandler {

	static FactoryPanelPosition connectingFrom;

	public static boolean panelClicked(LevelAccessor level, Player player, FactoryPanelPosition panelPos) {
		if (connectingFrom == null)
			return false;
		FactoryPanelBehaviour at = FactoryPanelBehaviour.at(level, connectingFrom);
		if (at == null) {
			connectingFrom = null;
			return false;
		}
//		at.displayScreen(player);
		AllPackets.getChannel()
			.sendToServer(new FactoryPanelConnectionPacket(panelPos, connectingFrom));
		connectingFrom = null;
		return true;
	}

}
