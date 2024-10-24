package com.simibubi.create.content.logistics.packagerLink;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

public class LogisticsNetwork {
	
	public UUID id;
	public RequestPromiseQueue panelPromises;
	public int totalLinks;
	public int loadedLinks;
	
	public LogisticsNetwork(UUID networkId) {
		id = networkId;
		panelPromises = new RequestPromiseQueue();
		totalLinks = 0;
		loadedLinks = 0;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("Id", id);
		tag.put("Promises", panelPromises.write());
		tag.putInt("Links", totalLinks);
		return tag;
	}
	
	public static LogisticsNetwork read(CompoundTag tag) {
		LogisticsNetwork network = new LogisticsNetwork(tag.getUUID("Id"));
		network.panelPromises = RequestPromiseQueue.read(tag.getCompound("Promises"));
		network.totalLinks = tag.getInt("Links");
		return network;
	}

}
