package com.simibubi.create.content.logistics.packager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockResponsePacket;

import net.createmod.catnip.utility.IntAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

public class InventorySummary {

	public static final InventorySummary EMPTY = new InventorySummary();

	private Map<Item, List<IntAttached<ItemStack>>> items = new IdentityHashMap<>();
	private List<IntAttached<ItemStack>> stacksByCount;

	public void add(InventorySummary summary) {
		summary.items.forEach((i, list) -> list.forEach(this::add));
	}

	public void add(ItemStack stack) {
		add(stack, stack.getCount());
	}

	public void add(IntAttached<ItemStack> entry) {
		add(entry.getSecond(), entry.getFirst());
	}

	public void add(ItemStack stack, int count) {
		if (count == 0 || stack.isEmpty())
			return;

		List<IntAttached<ItemStack>> stacks = items.computeIfAbsent(stack.getItem(), $ -> Lists.newArrayList());
		for (IntAttached<ItemStack> existing : stacks) {
			ItemStack existingStack = existing.getSecond();
			if (ItemHandlerHelper.canItemStacksStack(existingStack, stack)) {
				existing.setFirst(existing.getFirst() + count);
				return;
			}
		}

		IntAttached<ItemStack> newEntry = IntAttached.with(count, stack);
		stacks.add(newEntry);
	}
	
	public int getCountOf(ItemStack stack) {
		List<IntAttached<ItemStack>> list = items.get(stack.getItem());
		if (list == null)
			return 0;
		for (IntAttached<ItemStack> entry : list)
			if (ItemHandlerHelper.canItemStacksStack(entry.getSecond(), stack))
				return entry.getFirst();
		return 0;
	}

	public List<IntAttached<ItemStack>> getStacksByCount() {
		if (stacksByCount == null) {
			stacksByCount = new ArrayList<>();
			items.forEach((i, list) -> list.forEach(stacksByCount::add));
			Collections.sort(stacksByCount, IntAttached.comparator());
		}
		return stacksByCount;
	}

	public void divideAndSendTo(ServerPlayer player, BlockPos pos) {
		List<IntAttached<ItemStack>> stacks = getStacksByCount();
		int remaining = stacks.size();
		
		List<IntAttached<ItemStack>> currentList = null;
		PacketTarget target = PacketDistributor.PLAYER.with(() -> player);

		if (stacks.isEmpty())
			AllPackets.getChannel()
				.send(target, new LogisticalStockResponsePacket(true, pos, Collections.emptyList()));

		for (IntAttached<ItemStack> entry : stacks) {
			if (currentList == null)
				currentList = new ArrayList<>(Math.min(100, remaining));

			currentList.add(entry);
			remaining--;

			if (remaining == 0)
				break;
			if (currentList.size() < 100)
				continue;

			AllPackets.getChannel()
				.send(target, new LogisticalStockResponsePacket(false, pos, currentList));
			currentList = null;
		}

		if (currentList != null)
			AllPackets.getChannel()
				.send(target, new LogisticalStockResponsePacket(true, pos, currentList));
	}

}
