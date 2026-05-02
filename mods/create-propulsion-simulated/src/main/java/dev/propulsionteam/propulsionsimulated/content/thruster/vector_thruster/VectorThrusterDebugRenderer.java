package dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster;

import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.content.thruster.SimulatedThrustAdapter;
import dev.propulsionteam.propulsionsimulated.debug.DebugRenderer;
import dev.propulsionteam.propulsionsimulated.debug.PropulsionDebug;
import dev.propulsionteam.propulsionsimulated.debug.routes.MainDebugRoute;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public final class VectorThrusterDebugRenderer {
    private static final int RENDER_TICKS = 2;
    private static final float LINE_THICKNESS = 0.07f;
    private static final double STEP = 0.1d;
    private static final double START_EPSILON = 0.05d;

    private VectorThrusterDebugRenderer() {
    }

    public static void render(VectorThrusterBlockEntity be) {
        if (be == null || be.isRemoved() || be.getLevel() == null) {
            return;
        }
        if (!PropulsionDebug.isDebug(MainDebugRoute.THRUSTER)) {
            return;
        }

        Level level = be.getLevel();
        Vec3 localNozzle = be.getParticleDebugNozzlePositionLocal();
        Vec3 localExhaust = be.getParticleDebugExhaustDirectionLocal();
        if (localExhaust.lengthSqr() < 1.0e-8) {
            return;
        }

        int obstructionLength = PropulsionConfig.OBSTRUCTION_SCAN_LENGTH.get();
        Vec3 traceStart = localNozzle.add(localExhaust.scale(START_EPSILON));
        Vec3 localEnd = traceStart.add(localExhaust.scale(obstructionLength));

        Vec3 worldNozzle = Sable.HELPER.projectOutOfSubLevel(level, localNozzle);
        Vec3 worldEnd = Sable.HELPER.projectOutOfSubLevel(level, localEnd);

        String idBase = "vector_thruster_debug_" + be.getBlockPos().asLong();

        DebugRenderer.drawElongatedBox(
            idBase + "_ray",
            worldNozzle,
            worldEnd,
            LINE_THICKNESS,
            new Color(0, 255, 255, 255),
            false,
            RENDER_TICKS
        );
        DebugRenderer.drawBox(idBase + "_origin", worldNozzle, new Vec3(0.12, 0.12, 0.12), Color.GREEN, RENDER_TICKS);

        Set<Long> sampled = new HashSet<>();
        int hitIndex = 0;
        BlockPos selfPos = be.getBlockPos();
        long lastPosKey = Long.MIN_VALUE;
        for (double t = 0.0d; t <= obstructionLength; t += STEP) {
            Vec3 sample = traceStart.add(localExhaust.scale(t));
            BlockPos hitPos = BlockPos.containing(sample);
            long key = hitPos.asLong();
            if (key == lastPosKey) {
                continue;
            }
            lastPosKey = key;
            if (hitPos.equals(selfPos) || !sampled.add(key)) {
                continue;
            }

            BlockState stateAt = SimulatedThrustAdapter.getBlockStateSafe(level, hitPos);
            if (stateAt.isAir() || !stateAt.isSolid()) {
                continue;
            }

            Vec3 worldHitCenter = Sable.HELPER.projectOutOfSubLevel(level, Vec3.atCenterOf(hitPos));
            DebugRenderer.drawBox(
                idBase + "_hit_" + hitIndex,
                worldHitCenter,
                new Vec3(0.98, 0.98, 0.98),
                new Color(255, 64, 64, 255),
                RENDER_TICKS
            );
            hitIndex++;
        }
    }
}
