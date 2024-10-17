package com.simibubi.create.content.equipment.armor;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class CardboardHelmetItem extends BaseArmorItem {

	public CardboardHelmetItem(ArmorMaterial armorMaterial, Type type, Properties properties,
		ResourceLocation textureLoc) {
		super(armorMaterial, type, properties, textureLoc);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		super.initializeClient(consumer);
		consumer.accept(new CardboardArmorStealthOverlay());
	}

}
