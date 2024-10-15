package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.AllItems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CardboardArmorHandler {

	@SubscribeEvent
	public static void playersStealthWhenWearingCardboard(LivingVisibilityEvent event) {
		LivingEntity entity = event.getEntity();
		if (!testForStealth(entity))
			return;
		event.modifyVisibility(0);
	}

	@SubscribeEvent
	public static void mobsLoseTargetWhenItsWearingCardboard(LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.tickCount % 16 != 0)
			return;
		if (!(entity instanceof Mob mob))
			return;
		LivingEntity target = mob.getTarget();
		if (target == null || !testForStealth(target))
			return;
		mob.setTarget(null);
	}

	public static boolean testForStealth(LivingEntity entity) {
		if (entity.getPose() != Pose.CROUCHING)
			return false;
		if (!AllItems.CARDBOARD_HELMET.isIn(entity.getItemBySlot(EquipmentSlot.HEAD)))
			return false;
		if (!AllItems.CARDBOARD_CHESTPLATE.isIn(entity.getItemBySlot(EquipmentSlot.CHEST)))
			return false;
		if (!AllItems.CARDBOARD_LEGGINGS.isIn(entity.getItemBySlot(EquipmentSlot.LEGS)))
			return false;
		if (!AllItems.CARDBOARD_BOOTS.isIn(entity.getItemBySlot(EquipmentSlot.FEET)))
			return false;
		return true;
	}

}
