package com.simibubi.create.content.logistics.packagerLink;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class GlobalLogisticsManager {

	public Map<UUID, LogisticsNetwork> logisticsNetworks;

	private LogisticsNetworkSavedData savedData;

	public GlobalLogisticsManager() {
		logisticsNetworks = new HashMap<>();
	}

	public void levelLoaded(LevelAccessor level) {
		MinecraftServer server = level.getServer();
		if (server == null || server.overworld() != level)
			return;
		logisticsNetworks = new HashMap<>();
		savedData = null;
		loadLogisticsData(server);
	}

	public void linkAdded(UUID networkId) {
		logisticsNetworks.computeIfAbsent(networkId, $ -> new LogisticsNetwork(networkId)).totalLinks++;
		markDirty();
	}

	public void linkLoaded(UUID networkId) {
		logisticsNetworks.computeIfAbsent(networkId, $ -> new LogisticsNetwork(networkId)).loadedLinks++;
	}

	public void linkRemoved(UUID networkId) {
		LogisticsNetwork logisticsNetwork = logisticsNetworks.get(networkId);
		if (logisticsNetwork == null)
			return;
		logisticsNetwork.totalLinks--;
		if (logisticsNetwork.totalLinks <= 0)
			logisticsNetworks.remove(networkId);
		markDirty();
	}

	public void linkInvalidated(UUID networkId) {
		LogisticsNetwork logisticsNetwork = logisticsNetworks.get(networkId);
		if (logisticsNetwork == null)
			return;
		logisticsNetwork.loadedLinks--;
	}

	public int getUnloadedLinkCount(UUID networkId) {
		LogisticsNetwork logisticsNetwork = logisticsNetworks.get(networkId);
		if (logisticsNetwork == null)
			return 0;
		return logisticsNetwork.totalLinks - logisticsNetwork.loadedLinks;
	}

	public RequestPromiseQueue getQueuedPromises(UUID networkId) {
		return !logisticsNetworks.containsKey(networkId) ? null : logisticsNetworks.get(networkId).panelPromises;
	}

	public boolean hasQueuedPromises(UUID networkId) {
		return logisticsNetworks.containsKey(networkId) && !logisticsNetworks.get(networkId).panelPromises.isEmpty();
	}

	private void loadLogisticsData(MinecraftServer server) {
		if (savedData != null)
			return;
		savedData = LogisticsNetworkSavedData.load(server);
		logisticsNetworks = savedData.getLogisticsNetworks();
	}

	public void tick(Level level) {
		if (level.dimension() != Level.OVERWORLD)
			return;
		logisticsNetworks.forEach((id, network) -> {
			network.panelPromises.tick();
		});
	}

	public void markDirty() {
		if (savedData != null)
			savedData.setDirty();
	}

}
