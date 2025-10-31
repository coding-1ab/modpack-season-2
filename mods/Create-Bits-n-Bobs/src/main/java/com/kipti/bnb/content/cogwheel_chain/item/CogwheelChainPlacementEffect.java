package com.kipti.bnb.content.cogwheel_chain.item;

import com.kipti.bnb.content.cogwheel_chain.graph.PartialCogwheelChain;
import com.kipti.bnb.content.cogwheel_chain.graph.PartialCogwheelChainNode;
import com.kipti.bnb.registry.BnbDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CogwheelChainPlacementEffect {

    private static final float PARTICLE_DENSITY = 0.1f;

    public static void tick(LocalPlayer player) {
        if (Minecraft.getInstance().isPaused() || Minecraft.getInstance().hitResult == null) return;

        //Get held item
        ItemStack heldItem = player.getMainHandItem().getItem() instanceof CogwheelChainItem ? player.getMainHandItem() :
            player.getOffhandItem().getItem() instanceof CogwheelChainItem ? player.getOffhandItem() : null;
        if (heldItem != null) {
            display(player, heldItem);
        }
    }

    private static void display(LocalPlayer player, ItemStack heldItem) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        PartialCogwheelChain chain = heldItem.get(BnbDataComponents.PARTIAL_COGWHEEL_CHAIN);
        if (chain == null) {
            return;
        }

        HitResult genericHit = Minecraft.getInstance().hitResult;
        if (!(genericHit instanceof BlockHitResult hit)) {
            return;
        }

        //Get last node to calculate a chain preview
        PartialCogwheelChainNode lastNode = chain.getLastNode();
        Vec3 lastNodePos = Vec3.atCenterOf(lastNode.pos());
        Direction.Axis axis = lastNode.rotationAxis();

        //Project the current targeted position onto the plane defined by the last node's axis
        Vec3 targetedOrigin = hit.getLocation();
        Vec3 toTargeted = targetedOrigin.subtract(lastNodePos);

        Vec3 axisNormal = Vec3.atLowerCornerOf(Direction.get(Direction.AxisDirection.POSITIVE, axis).getNormal());
        Vec3 projected = toTargeted.subtract(axisNormal.scale(toTargeted.dot(axisNormal))).add(lastNodePos);

        Vec3 lastPos = chain.getNodeCenter(0);
        for (int i = 1; i < chain.getSize(); i++) {
            Vec3 currentPos = chain.getNodeCenter(i);
            renderParticlesBetween(level, lastPos, currentPos, true);
            lastPos = currentPos;
        }
        renderParticlesBetween(level, lastPos, projected, true);
    }

    private static void renderParticlesBetween(ClientLevel level, Vec3 from, Vec3 to, boolean valid) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        Vec3 dir = delta.normalize();
        double step = 0.25;
        for (double t = 0; t <= length; t += step) {
            if (level.getRandom().nextFloat() > PARTICLE_DENSITY) {
                continue;
            }
            Vec3 lerped = from.add(dir.scale(t));
            level.addParticle(
                new DustParticleOptions(new Vector3f(valid ? .3f : .9f, valid ? .9f : .3f, .5f), 1), true,
                lerped.x, lerped.y, lerped.z, 0, 0, 0);
        }
    }

}
