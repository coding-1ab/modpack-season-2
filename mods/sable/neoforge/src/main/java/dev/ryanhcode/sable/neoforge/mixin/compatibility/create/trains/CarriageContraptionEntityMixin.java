package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.trains;

import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes the range check in Create's {@link CarriageContraptionEntity} control handling to account for sub-levels.
 */
@Mixin(CarriageContraptionEntity.class)
public class CarriageContraptionEntityMixin extends OrientedContraptionEntity {
    public CarriageContraptionEntityMixin(final EntityType<?> type, final Level world) {
        super(type, world);
    }

    @Redirect(method = "control", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z"))
    private boolean sable$projectComparison(final Vec3 controllerPos, final Position playerPos, final double pDistance) {
        return Sable.HELPER.distanceSquaredWithSubLevels(this.level(), controllerPos, playerPos.x(), playerPos.y(), playerPos.z()) < pDistance * pDistance;
    }
}
