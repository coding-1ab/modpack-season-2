package com.kipti.bnb.content.cogwheel_chain.item;

import com.kipti.bnb.content.cogwheel_chain.graph.PartialCogwheelChainNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import static com.kipti.bnb.content.cogwheel_chain.item.CogwheelChainPlacementInteraction.currentBuildingChain;
import static com.kipti.bnb.content.cogwheel_chain.item.CogwheelChainPlacementInteraction.currentChainLevel;

public class CogwheelChainPlacementEffect {

    private static final float PARTICLE_DENSITY = 0.1f;

    public static void tick(LocalPlayer player) {
        if (Minecraft.getInstance().isPaused() || Minecraft.getInstance().hitResult == null) return;

        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null || currentChainLevel == null || currentBuildingChain == null) {
            return;
        }
        if (!currentChainLevel.equals(level.dimension())) {
            currentBuildingChain = null;
            currentChainLevel = null;
        }

        //Get held chain
        final ItemStack heldItem = isChain(player.getMainHandItem()) ? player.getMainHandItem() :
                isChain(player.getOffhandItem()) ? player.getOffhandItem() : null;
        if (heldItem != null) {
            display();
        }
    }

    private static boolean isChain(final ItemStack offhandItem) {
        return offhandItem.is(Items.CHAIN);
    }

    private static void display() {
        if (currentBuildingChain == null)
            return;

        final ClientLevel level = Minecraft.getInstance().level;

        final HitResult genericHit = Minecraft.getInstance().hitResult;
        if (!(genericHit instanceof BlockHitResult hit)) {
            return;
        }

        //Get last chainNode to calculate a chain preview
        final PartialCogwheelChainNode lastNode = currentBuildingChain.getLastNode();
        final Vec3 lastNodePos = Vec3.atCenterOf(lastNode.pos());
        final Direction.Axis axis = lastNode.rotationAxis();

        //Project the current targeted position onto the plane defined by the last chainNode's axis
        final Vec3 targetedOrigin = hit.getLocation();
        final Vec3 toTargeted = targetedOrigin.subtract(lastNodePos);

        final Vec3 axisNormal = Vec3.atLowerCornerOf(Direction.get(Direction.AxisDirection.POSITIVE, axis).getNormal());
        final Vec3 projected = toTargeted.subtract(axisNormal.scale(toTargeted.dot(axisNormal))).add(lastNodePos);

        Vec3 lastPos = currentBuildingChain.getNodeCenter(0);
        for (int i = 1; i < currentBuildingChain.getSize(); i++) {
            final Vec3 currentPos = currentBuildingChain.getNodeCenter(i);
            renderParticlesBetween(level, lastPos, currentPos, true);
            lastPos = currentPos;
        }
        renderParticlesBetween(level, lastPos, projected, true);
    }

    private static void renderParticlesBetween(final ClientLevel level, final Vec3 from, final Vec3 to, boolean valid) {
        final Vec3 delta = to.subtract(from);
        final double length = delta.length();
        final Vec3 dir = delta.normalize();
        final double step = 0.25;

        for (double t = 0; t <= length; t += step) {
            if (level.getRandom().nextFloat() > PARTICLE_DENSITY) {
                continue;
            }
            final Vec3 lerped = from.add(dir.scale(t));
            level.addParticle(
                    new DustParticleOptions(new Vector3f(valid ? .3f : .9f, valid ? .9f : .3f, .5f), 1), true,
                    lerped.x, lerped.y, lerped.z, 0, 0, 0);
        }
    }

}
