package com.simibubi.create.content.equipment.tool;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CardboardSwordItem extends SwordItem {

	private ImmutableMultimap<Attribute, AttributeModifier> cardboardModifiers;

	public CardboardSwordItem(Properties pProperties) {
		super(AllToolMaterials.CARDBOARD, 3, 1f, pProperties);

		float attackDamage = 3f + AllToolMaterials.CARDBOARD.getAttackDamageBonus();
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
			attackDamage, AttributeModifier.Operation.ADDITION));
		builder.put(Attributes.ATTACK_SPEED,
			new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 1f, AttributeModifier.Operation.ADDITION));

		// Setting knockback to 0 prevents the aggressive punching sound
		// Knockback strength is reapplied in the event handler below
		builder.put(Attributes.ATTACK_KNOCKBACK,
			new AttributeModifier(UUID.fromString("45227E1C-A180-4865-B01B-BCCE9785ACA3"), "Weapon modifier", 0f,
				AttributeModifier.Operation.MULTIPLY_TOTAL));

		cardboardModifiers = builder.build();
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
		return pEquipmentSlot == EquipmentSlot.MAINHAND ? cardboardModifiers
			: super.getDefaultAttributeModifiers(pEquipmentSlot);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment == Enchantments.KNOCKBACK;
	}

	@Override
	public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
		return TooltipPart.MODIFIERS.getMask();
	}

	@Override
	public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
		return super.hurtEnemy(pStack, pTarget, pAttacker);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		AllSoundEvents.CARDBOARD_SWORD.playFrom(entity, 0.75f, 1.5f);
		return super.onLeftClickEntity(stack, player, entity);
	}

	@SubscribeEvent
	public static void cardboardSwordsCannotHurtYou(LivingAttackEvent event) {
		Entity attacker = event.getSource()
			.getEntity();
		LivingEntity target = event.getEntity();
		if (target instanceof Spider)
			return;
		if (!(attacker instanceof LivingEntity livingAttacker
			&& AllItems.CARDBOARD_SWORD.isIn(livingAttacker.getItemInHand(InteractionHand.MAIN_HAND))))
			return;

		event.setCanceled(true);

		// Reference player.attack()
		// This section replicates knockback behaviour without hurting the target

		double i = livingAttacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK) + 2;
		i += EnchantmentHelper.getKnockbackBonus(livingAttacker);
		if (livingAttacker.isSprinting()
			&& (!(livingAttacker instanceof Player p) || p.getAttackStrengthScale(0.5f) > 0.9f))
			++i;

		if (i <= 0)
			return;

		if (target instanceof LivingEntity livingTarget) {
			livingTarget.knockback(i * 0.5F, Mth.sin(livingAttacker.getYRot() * Mth.DEG_TO_RAD),
				-Mth.cos(livingAttacker.getYRot() * Mth.DEG_TO_RAD));
			if ((livingTarget.getClassification(false) == MobCategory.MISC
				|| livingTarget.getClassification(false) == MobCategory.CREATURE) && !(livingTarget instanceof Player))
				livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9, true, false, false));

		} else {
			event.getEntity()
				.push(-Mth.sin(livingAttacker.getYRot() * Mth.DEG_TO_RAD) * i * 0.5F, 0.05D,
					Mth.cos(livingAttacker.getYRot() * Mth.DEG_TO_RAD) * i * 0.5F);
		}

		livingAttacker.setDeltaMovement(livingAttacker.getDeltaMovement()
			.multiply(0.6D, 1.0D, 0.6D));
		livingAttacker.setSprinting(false);
	}

}
