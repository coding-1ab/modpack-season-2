package com.simibubi.create.content.logistics.packagerLink;

import java.util.UUID;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.createmod.catnip.CatnipClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LogisticallyLinkedClientHandler {

	public static void tick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		ItemStack mainHandItem = player.getMainHandItem();
		if (!(mainHandItem.getItem() instanceof LogisticallyLinkedBlockItem)
			|| !LogisticallyLinkedBlockItem.isTuned(mainHandItem))
			return;

		CompoundTag tag = mainHandItem.getTag()
			.getCompound(BlockItem.BLOCK_ENTITY_TAG);
		if (!tag.hasUUID("Freq"))
			return;

		UUID uuid = tag.getUUID("Freq");
		for (LogisticallyLinkedBehaviour behaviour : LogisticallyLinkedBehaviour.getAllPresent(uuid, false)) {
			SmartBlockEntity be = behaviour.blockEntity;
			VoxelShape shape = be.getBlockState()
				.getShape(player.level(), be.getBlockPos());
			if (shape.isEmpty())
				continue;
			CatnipClient.OUTLINER.showAABB(behaviour, shape.bounds()
				.inflate(AllBlockEntityTypes.FACTORY_PANEL.is(be) ? -1 / 16f : 0)
				.move(be.getBlockPos()), 2)
				.lineWidth(1 / 16f);
		}
	}

}
