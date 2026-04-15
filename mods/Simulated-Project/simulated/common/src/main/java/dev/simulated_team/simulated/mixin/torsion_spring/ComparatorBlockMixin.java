package dev.simulated_team.simulated.mixin.torsion_spring;

import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelBlock;
import dev.simulated_team.simulated.content.blocks.torsion_spring.TorsionSpringBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ComparatorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComparatorBlock.class)
public class ComparatorBlockMixin {
    @Inject(method = "getInputSignal", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private void rememberMostRecent(final CallbackInfoReturnable<Integer> cir, @Local final Direction direction) {
        TorsionSpringBlock.comparatorDir = direction;
        SteeringWheelBlock.comparatorDir = direction;
    }
}
