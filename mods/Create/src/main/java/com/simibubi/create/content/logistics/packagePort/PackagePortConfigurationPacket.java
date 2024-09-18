package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PackagePortConfigurationPacket extends BlockEntityConfigurationPacket<PackagePortBlockEntity> {

	private String newFilter;
	private boolean ejectPackages;
	private boolean acceptPackages;

	public PackagePortConfigurationPacket(BlockPos pos, String newFilter, boolean acceptPackages, boolean ejectPackages) {
		super(pos);
		this.newFilter = newFilter;
		this.ejectPackages = ejectPackages;
		this.acceptPackages = acceptPackages;
	}

	public PackagePortConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeBoolean(ejectPackages);
		buffer.writeBoolean(acceptPackages);
		buffer.writeUtf(newFilter);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		ejectPackages = buffer.readBoolean();
		acceptPackages = buffer.readBoolean();
		newFilter = buffer.readUtf();
	}

	@Override
	protected void applySettings(ServerPlayer player, PackagePortBlockEntity be) {
		super.applySettings(player, be);
		if (!ejectPackages)
			return;
		for (int i = 0; i < be.inventory.getSlots(); i++) {
			ItemStack stackInSlot = be.inventory.getStackInSlot(i);
			if (stackInSlot.isEmpty())
				continue;
			player.getInventory()
				.placeItemBackInInventory(stackInSlot);
			be.inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
		be.notifyUpdate();
	}

	@Override
	protected void applySettings(PackagePortBlockEntity be) {
		if (be.addressFilter.equals(newFilter) && be.acceptsPackages == acceptPackages)
			return;
		be.addressFilter = newFilter;
		be.acceptsPackages = acceptPackages;
		be.filterChanged();
		be.notifyUpdate();
	}

}
