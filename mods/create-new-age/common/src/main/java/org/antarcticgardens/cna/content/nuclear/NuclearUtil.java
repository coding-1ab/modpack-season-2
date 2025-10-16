package org.antarcticgardens.cna.content.nuclear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.*;
import org.antarcticgardens.cna.CNATags;
import org.antarcticgardens.cna.util.RaycastUtil;

import java.util.List;

public class NuclearUtil {
    public static void createRadiation(int length, Level world, BlockPos pos) {
        if (world.isClientSide())
            return;

        List<LivingEntity> entities = world.getEntities(EntityTypeTest.forClass(LivingEntity.class), new AABB(pos).inflate(length),
                livingEntity -> !isResistant(livingEntity));

        for (LivingEntity le : entities) {
            for (Direction dir : Direction.values()) {
                if (world.getBlockState(pos.relative(dir)).is(CNATags.Block.STOPS_RADIATION.blockTag))
                    continue;

                Vec3 start = pos.getCenter().relative(dir, 0.5f);
                double distance = le.getEyePosition().distanceTo(start);

                if (distance > length)
                    continue;

                Vec3 direction = le.getEyePosition().subtract(start).normalize();
                HitResult hitResult = RaycastUtil.pickFilteredBlockFromPos(world, start, direction, (float) Math.ceil(distance), bs -> bs.is(CNATags.Block.STOPS_RADIATION.blockTag));

                if (hitResult instanceof BlockHitResult bhr) {
                    if (world.getBlockState(bhr.getBlockPos()).is(CNATags.Block.STOPS_RADIATION.blockTag))
                        continue;

                    if (bhr.getLocation().distanceTo(start) < distance)
                        continue;
                }

                irradiate(le);
                break;
            }
        }
    }

    private static boolean isResistant(LivingEntity entity) {
        if (entity instanceof Player pl && (pl.isCreative() || pl.isSpectator()))
            return true;

        for (ItemStack piece : entity.getArmorSlots()) {
            if (!piece.is(CNATags.Item.HAZMAT_SUIT.tag))
                return false;
        }

        return true;
    }

    private static void irradiate(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1));
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 1));
        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
    }
}
