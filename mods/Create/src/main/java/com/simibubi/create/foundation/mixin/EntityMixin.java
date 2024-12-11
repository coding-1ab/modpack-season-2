package com.simibubi.create.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;

import net.minecraft.world.entity.Pose;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;

import net.minecraft.world.entity.Entity;

@Mixin(value = Entity.class, priority = 900)
public class EntityMixin {
	@ModifyExpressionValue(method = "canEnterPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
	public boolean create$playerHidingAsBoxIsCrouchingNotSwimming(boolean original, @Local(argsOnly = true) Pose pose) {
		return original || (pose == Pose.CROUCHING && CardboardArmorHandler.testForStealth((Entity) (Object) this));
	}

	@Inject(method = "fireImmune()Z", at = @At("RETURN"), cancellable = true)
	public void create$onFireImmune(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			Entity self = (Entity) (Object) this;
			boolean immune = self.getPersistentData().getBoolean(NetheriteDivingHandler.FIRE_IMMUNE_KEY);
			if (immune)
				cir.setReturnValue(immune);
		}
	}
}
