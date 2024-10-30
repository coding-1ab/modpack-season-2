package com.simibubi.create.content.logistics.displayCloth;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.blockEntity.RemoveBlockEntityPacket;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DisplayClothBlockEntity extends SmartBlockEntity {

	private List<ItemStack> manuallyAddedItems;

	public DisplayClothBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		manuallyAddedItems = new ArrayList<>();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	public List<ItemStack> getItemsForRender() {
		return manuallyAddedItems;
	}

	public InteractionResult use(Player player) {
		ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

		if (heldItem.isEmpty()) {
			if (manuallyAddedItems.isEmpty())
				return InteractionResult.SUCCESS;
			player.setItemInHand(InteractionHand.MAIN_HAND, manuallyAddedItems.remove(manuallyAddedItems.size() - 1));

			if (manuallyAddedItems.isEmpty()) {
				level.setBlock(worldPosition, getBlockState().setValue(DisplayClothBlock.HAS_BE, false), 3);
				AllPackets.getChannel()
					.send(packetTarget(), new RemoveBlockEntityPacket(worldPosition));
			} else
				notifyUpdate();

			return InteractionResult.SUCCESS;
		}

		if (manuallyAddedItems.size() >= 4)
			return InteractionResult.SUCCESS;

		manuallyAddedItems.add(heldItem.copyWithCount(1));
		heldItem.shrink(1);
		if (heldItem.isEmpty())
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		notifyUpdate();
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.put("Items", NBTHelper.writeItemList(manuallyAddedItems));
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		manuallyAddedItems = NBTHelper.readItemList(tag.getList("Items", Tag.TAG_COMPOUND));
	}

	@Override
	public void destroy() {
		super.destroy();
		manuallyAddedItems.forEach(stack -> Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
			worldPosition.getZ(), stack));
		manuallyAddedItems.clear();
	}

}
