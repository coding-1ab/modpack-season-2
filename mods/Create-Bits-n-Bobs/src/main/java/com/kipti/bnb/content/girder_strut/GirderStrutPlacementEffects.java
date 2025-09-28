package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class GirderStrutPlacementEffects {

    public static void tick(LocalPlayer player) {
        if (Minecraft.getInstance().isPaused()) return;

        //Get held item
        ItemStack heldItem = player.getMainHandItem().getItem() instanceof GirderStrutBlockItem girderStrutMainHand ? player.getMainHandItem() :
            player.getOffhandItem().getItem() instanceof GirderStrutBlockItem girderStrutOffHand ? player.getOffhandItem() : null;
        if (heldItem != null) {
            display(player, heldItem);
        }
    }

    private static void display(LocalPlayer player, ItemStack heldItem) {
        ClientLevel level = Minecraft.getInstance().level;
        boolean valid = true;
        BlockPos fromPos = heldItem.get(BnbDataComponents.GIRDER_STRUT_FROM);

        if (fromPos == null) {
            return;
        }

        BlockPos targetPos = BlockPos.containing(Minecraft.getInstance().hitResult.getLocation());

        Vec3 renderFrom = Vec3.atCenterOf(fromPos);
        Vec3 renderTo = Vec3.atCenterOf(targetPos);

        Vec3 dir = renderTo.subtract(renderFrom).normalize();

        for (double t = 0; t <= renderFrom.length(); t += 0.1) {
            Vec3 lerped = renderFrom.add(dir.scale(t));

            level.addParticle(
                new DustParticleOptions(new Vector3f(valid ? .3f : .9f, valid ? .9f : .3f, .5f), 1), true,
                lerped.x + .5f, lerped.y + .5f, lerped.z + .5f, 0, 0, 0);
        }

    }

}
