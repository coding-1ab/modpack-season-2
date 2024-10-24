package com.simibubi.create.content.logistics;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;

import net.createmod.catnip.utility.IntAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AddressEditBoxHelper {

	private static Cache<BlockPos, WeakReference<ClipboardBlockEntity>> NEARBY_CLIPBOARDS = CacheBuilder.newBuilder()
		.expireAfterWrite(1, TimeUnit.SECONDS)
		.build();

	public static void advertiseClipboard(ClipboardBlockEntity blockEntity) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null)
			return;
		BlockPos blockPos = blockEntity.getBlockPos();
		if (player.distanceToSqr(Vec3.atCenterOf(blockPos)) > 32 * 32)
			return;
		NEARBY_CLIPBOARDS.put(blockPos, new WeakReference<>(blockEntity));
	}

	public static DestinationSuggestions createSuggestions(Screen screen, EditBox pInput) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		List<IntAttached<String>> options = new ArrayList<>();
		DestinationSuggestions destinationSuggestions =
			new DestinationSuggestions(mc, screen, pInput, mc.font, options, -72 + pInput.getY() + pInput.getHeight());

		if (player == null)
			return destinationSuggestions;

		for (int i = 0; i < Inventory.INVENTORY_SIZE; i++)
			appendAddresses(options, player.getInventory()
				.getItem(i));

		for (WeakReference<ClipboardBlockEntity> wr : NEARBY_CLIPBOARDS.asMap()
			.values()) {
			ClipboardBlockEntity cbe = wr.get();
			if (cbe != null)
				appendAddresses(options, cbe.dataContainer);
		}

		return destinationSuggestions;
	}

	private static void appendAddresses(List<IntAttached<String>> options, ItemStack item) {
		if (item == null || !AllBlocks.CLIPBOARD.isIn(item))
			return;

		List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(item);
		pages.forEach(page -> page.forEach(entry -> {
			String string = entry.text.getString();
			if (entry.checked)
				return;
			if (!string.startsWith("#") || string.length() <= 1)
				return;
			String address = string.substring(1);
			if (address.isBlank())
				return;
			options.add(IntAttached.withZero(address.trim()));
		}));
	}

}
