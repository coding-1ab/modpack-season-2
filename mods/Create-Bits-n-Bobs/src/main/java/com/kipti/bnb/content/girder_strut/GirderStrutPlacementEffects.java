package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class GirderStrutPlacementEffects {

    private static final float PARTICLE_DENSITY = 0.3f;

    public static void tick(LocalPlayer player) {
        if (Minecraft.getInstance().isPaused() || Minecraft.getInstance().hitResult == null) return;

        //Get held item
        ItemStack heldItem = player.getMainHandItem().getItem() instanceof GirderStrutBlockItem girderStrutMainHand ? player.getMainHandItem() :
            player.getOffhandItem().getItem() instanceof GirderStrutBlockItem girderStrutOffHand ? player.getOffhandItem() : null;
        if (heldItem != null) {
            display(player, heldItem);
        }
    }

    private static void display(LocalPlayer player, ItemStack heldItem) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockPos fromPos = heldItem.get(BnbDataComponents.GIRDER_STRUT_FROM);
        Direction fromFace = heldItem.get(BnbDataComponents.GIRDER_STRUT_FROM_FACE);

        if (fromPos == null) {
            return;
        }

        HitResult genericHit = Minecraft.getInstance().hitResult;
        if (!(genericHit instanceof BlockHitResult hit)) {
            return;
        }

        BlockPos targetPos = resolvePlacementPos(level, hit.getBlockPos(), hit.getDirection());
        if (targetPos == null) {
            return;
        }

        Direction targetFace = hit.getDirection();

        Vec3 renderFrom = Vec3.atCenterOf(fromPos);
        Vec3 renderTo = Vec3.atCenterOf(targetPos);

        Vec3 delta = renderTo.subtract(renderFrom);
        double length = delta.length();
        if (length < 1.0E-3) {
            return;
        }

        boolean valid = GirderStrutBlockItem.isValidConnection(level, fromPos, fromFace, targetPos, targetFace);

        Vec3 dir = delta.normalize();
        double step = 0.25;
        for (double t = 0; t <= length; t += step) {
            Vec3 lerped = renderFrom.add(dir.scale(t));

            if (level.getRandom().nextFloat() > PARTICLE_DENSITY) {
                continue;
            }

            level.addParticle(
                new DustParticleOptions(new Vector3f(valid ? .3f : .9f, valid ? .9f : .3f, .5f), 1), true,
                lerped.x, lerped.y, lerped.z, 0, 0, 0);
        }
        level.addParticle(
            new DustParticleOptions(new Vector3f(valid ? .3f : .9f, valid ? .9f : .3f, .5f), 1), true,
            renderTo.x, renderTo.y, renderTo.z, 0, 0, 0);

    }

    private static BlockPos resolvePlacementPos(ClientLevel level, BlockPos clickedPos, Direction face) {
        BlockPos pos = clickedPos;
        if (!(level.getBlockState(pos).getBlock() instanceof GirderStrutBlock)) {
            pos = pos.relative(face);
            if (!(level.getBlockState(pos).isAir() || level.getBlockState(pos).getBlock() instanceof GirderStrutBlock)) {
                return null;
            }
        }
        return pos;
    }

}
