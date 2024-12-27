package com.simibubi.create.content.logistics.box;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.Create;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PackageStyles {

	public static record PackageStyle(String type, int width, int height, float riggingOffset, boolean rare) {
		
		public ResourceLocation getItemId() {
			String size = "_" + width + "x" + height;
			String id = type + "_package" + (rare ? "" : size);
			return Create.asResource(id);
		}
		
		public ResourceLocation getRiggingModel() {
			String size = width + "x" + height;
			return Create.asResource("item/package/rigging_" + size);
		}
		
	};

	public static final List<PackageStyle> STYLES = new ArrayList<>(List.of(

		new PackageStyle("cardboard", 12, 12, 23f, false),
		new PackageStyle("cardboard", 10, 12, 22f, false),
		new PackageStyle("cardboard", 10, 8, 18f, false),
		new PackageStyle("cardboard", 12, 10, 21f, false),

		new PackageStyle("rare_kryppers", 12, 10, 21f, true),
		new PackageStyle("rare_jinx", 12, 10, 21f, true),
		new PackageStyle("rare_creeper", 12, 10, 21f, true),
		new PackageStyle("rare_simi", 12, 10, 21f, true)

	));

	public static final List<PackageItem> ALL_BOXES = new ArrayList<>();
	public static final List<PackageItem> STANDARD_BOXES = new ArrayList<>();
	public static final List<PackageItem> RARE_BOXES = new ArrayList<>();

	private static final Random STYLE_PICKER = new Random();
	private static final int RARE_CHANCE = 5000; // addons, have mercy

	public static ItemStack getRandomBox() {
		List<PackageItem> pool = STYLE_PICKER.nextInt(RARE_CHANCE) == 0 ? RARE_BOXES : STANDARD_BOXES;
		return new ItemStack(pool.get(STYLE_PICKER.nextInt(pool.size())));
	}
	
	public static ItemStack getDefaultBox() {
		return new ItemStack(ALL_BOXES.get(0));
	}

}
