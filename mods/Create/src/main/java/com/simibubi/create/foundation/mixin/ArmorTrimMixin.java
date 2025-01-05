package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;

@Mixin(ArmorTrim.class)
public class ArmorTrimMixin {
	@ModifyExpressionValue(method = {"lambda$new$2", "lambda$new$4"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/armortrim/TrimPattern;assetId()Lnet/minecraft/resources/ResourceLocation;"))
	private ResourceLocation create$swapTexturesForCardboardTrims(ResourceLocation original, @Local(argsOnly = true) ArmorMaterial armorMaterial) {
		if (armorMaterial == AllArmorMaterials.CARDBOARD)
			return Create.asResource("card_" + original.getPath());
		return original;
	}
}
