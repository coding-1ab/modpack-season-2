package com.simibubi.create.content.logistics.packagerLink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.BigItemStack;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class RequestPromiseQueue {

	private Map<Item, List<RequestPromise>> promisesByItem;

	public RequestPromiseQueue() {
		promisesByItem = new IdentityHashMap<>();
	}

	public void add(RequestPromise promise) {
		promisesByItem.computeIfAbsent(promise.promisedStack.stack.getItem(), $ -> new LinkedList<>())
			.add(promise);
		Create.LOGISTICS.markDirty();
	}

	public int getTotalPromisedAndRemoveExpired(ItemStack stack, int expiryTime) {
		int promised = 0;
		List<RequestPromise> list = promisesByItem.get(stack.getItem());
		if (list == null)
			return promised;

		for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext();) {
			RequestPromise promise = iterator.next();
			if (!ItemHandlerHelper.canItemStacksStack(promise.promisedStack.stack, stack))
				continue;
			if (expiryTime != -1 && promise.ticksExisted >= expiryTime) {
				iterator.remove();
				Create.LOGISTICS.markDirty();
				continue;
			}

			promised += promise.promisedStack.count;
		}
		return promised;
	}

	public void forceClear(ItemStack stack) {
		List<RequestPromise> list = promisesByItem.get(stack.getItem());
		if (list == null)
			return;

		for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext();) {
			RequestPromise promise = iterator.next();
			if (!ItemHandlerHelper.canItemStacksStack(promise.promisedStack.stack, stack))
				continue;
			iterator.remove();
			Create.LOGISTICS.markDirty();
		}

		if (list.isEmpty())
			promisesByItem.remove(stack.getItem());
	}

	public void itemEnteredSystem(ItemStack stack, int amount) {
		List<RequestPromise> list = promisesByItem.get(stack.getItem());
		if (list == null)
			return;

		for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext();) {
			RequestPromise requestPromise = iterator.next();
			if (!ItemHandlerHelper.canItemStacksStack(requestPromise.promisedStack.stack, stack))
				continue;

			int toSubtract = Math.min(amount, requestPromise.promisedStack.count);
			amount -= toSubtract;
			requestPromise.promisedStack.count -= toSubtract;

			if (requestPromise.promisedStack.count <= 0) {
				iterator.remove();
				Create.LOGISTICS.markDirty();
			}
			if (amount <= 0)
				break;
		}

		if (list.isEmpty())
			promisesByItem.remove(stack.getItem());
	}

	public List<RequestPromise> flatten(boolean sorted) {
		List<RequestPromise> all = new ArrayList<>();
		promisesByItem.forEach((key, list) -> all.addAll(list));
		if (sorted)
			Collections.sort(all, RequestPromise.ageComparator());
		return all;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.put("List", NBTHelper.writeCompoundList(flatten(false), rp -> {
			CompoundTag c = rp.promisedStack.write();
			c.putInt("Age", rp.ticksExisted);
			return c;
		}));
		return tag;
	}

	public static RequestPromiseQueue read(CompoundTag tag) {
		RequestPromiseQueue queue = new RequestPromiseQueue();
		NBTHelper.iterateCompoundList(tag.getList("List", Tag.TAG_COMPOUND), c -> {
			RequestPromise promise = new RequestPromise(BigItemStack.read(c));
			promise.ticksExisted = c.getInt("Age");
			queue.add(promise);
		});
		return queue;
	}

	public void tick() {
		promisesByItem.forEach((key, list) -> list.forEach(RequestPromise::tick)); // delete old entries?
	}

	public boolean isEmpty() {
		return promisesByItem.isEmpty();
	}

}
