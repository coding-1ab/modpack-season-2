package org.antarcticgardens.newage.content.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.antarcticgardens.newage.CreateNewAge;
import org.joml.Vector3f;

import java.util.Objects;

public class NuclearUtil {

    public static final TagKey<Block> STOPS_RADIATION = new TagKey<>(Registries.BLOCK, new ResourceLocation(CreateNewAge.MOD_ID, "stops_radiation"));
    public static final TagKey<Item> HAZMAT_SUIT = new TagKey<>(Registries.ITEM, new ResourceLocation(CreateNewAge.MOD_ID, "nuclear/hazmat_suit"));

    public static void createRadiation(int length, Level world, BlockPos pos) {
        if (world.isClientSide()) return;
        var rand = world.getRandom();
        Vector3f m = new Vector3f(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
        m.normalize();
        int total = 0;
        for (int dist = 0 ; dist < length ; dist++) {
            BlockPos p = pos.offset((int) (m.x*dist), (int) (m.y*dist), (int) (m.z*dist));
            if (world.getBlockState(p).is(STOPS_RADIATION) && !p.equals(pos)) {
                break;
            }
            total = dist;
        }
        var entities = world.getEntities(EntityTypeTest.forClass(LivingEntity.class), new AABB(pos.offset((int) (-total*Math.abs(m.x)), (int) (-total*Math.abs(m.y)), (int) (-total*Math.abs(m.z))), pos.offset((int) (total*Math.abs(m.x)), (int) (total*Math.abs(m.y)), (int) (total*Math.abs(m.z)))), Objects::nonNull);

        cycle: for (LivingEntity entity : entities) {
            if (entity instanceof Player pl && pl.isCreative()) continue cycle;
            for (ItemStack piece : entity.getArmorSlots()) {
                if (!piece.is(HAZMAT_SUIT)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                    continue cycle;
                }
            }
        }
    }
}
