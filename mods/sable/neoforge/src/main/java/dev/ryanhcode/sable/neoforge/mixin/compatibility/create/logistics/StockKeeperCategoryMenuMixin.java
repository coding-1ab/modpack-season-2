package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.logistics;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.Position;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StockKeeperCategoryMenu.class, remap = false)
public class StockKeeperCategoryMenuMixin {

    @Redirect(method = "stillValid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z"))
    private boolean sable$projectComparison(final Vec3 posA, final Position posB, final double pDistance, @Local(argsOnly = true) final Player player) {
        return Sable.HELPER.distanceSquaredWithSubLevels(player.level(), posA, posB) < pDistance * pDistance;
    }

}
