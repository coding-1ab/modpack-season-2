package com.simibubi.create.content.contraptions.sync;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent.Context;

public class ContraptionItemPacket extends SimplePacketBase {

	private int entityId;
	private BlockPos localPos;
	private List<ItemStack> containedItems;

	public ContraptionItemPacket(int entityId, BlockPos localPos, ItemStackHandler handler) {
		this.entityId = entityId;
		this.localPos = localPos;
		this.containedItems = new ArrayList<>(handler.getSlots());
		for (int i = 0; i < handler.getSlots(); i++)
			containedItems.add(handler.getStackInSlot(i));
	}

	public ContraptionItemPacket(FriendlyByteBuf buffer) {
		entityId = buffer.readInt();
		localPos = buffer.readBlockPos();
		int count = buffer.readVarInt();
		containedItems = new ArrayList<>();
		for (int i = 0; i < count; i++)
			containedItems.add(buffer.readItem());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityId);
		buffer.writeBlockPos(localPos);
		buffer.writeVarInt(containedItems.size());
		for (ItemStack stack : containedItems)
			buffer.writeItem(stack);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
			if (!(entityByID instanceof AbstractContraptionEntity))
				return;
			AbstractContraptionEntity contraptionEntity = (AbstractContraptionEntity) entityByID;
			contraptionEntity.getContraption().handleContraptionItemPacket(localPos, containedItems);
		});
		return true;
	}
}
