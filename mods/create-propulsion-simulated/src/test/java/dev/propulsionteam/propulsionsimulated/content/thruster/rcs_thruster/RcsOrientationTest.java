package dev.propulsionteam.propulsionsimulated.content.thruster.rcs_thruster;

import net.minecraft.core.Direction;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RcsOrientationTest {
    @Test
    void mountedModelAndPhysicsAgreeOnEveryFace() {
        for (Direction normal : Direction.values()) {
            assertEquals(normal, RcsOrientation.direction(Direction.UP, normal));
            for (Direction axis : Direction.values()) {
                Vector3d input = new Vector3d(axis.getStepX(), axis.getStepY(), axis.getStepZ());
                Vector3d expected = RcsOrientation.direction(input, normal);
                Vector3f rendered = RcsOrientation.rotation(normal).transform(new Vector3f((float) input.x, (float) input.y, (float) input.z));
                assertEquals(0, expected.distance(new Vector3d(rendered)), 1e-6);
            }
        }
    }

    @Test
    void singlePushesOppositeItsExhaustOnEveryFace() {
        for (Direction facing : Direction.values()) {
            Vector3d impulse = RcsOrientation.impulse(true, 0, facing.getOpposite(), 100, 0.05);
            Vector3d expected = new Vector3d(facing.getStepX(), facing.getStepY(), facing.getStepZ()).mul(5.0);
            assertEquals(0, expected.distance(impulse), 1e-12);
        }
    }

    @Test
    void opposingNozzlesCancelForceAndTorque() {
        for (Direction normal : Direction.values()) {
            Vector3d total = new Vector3d();
            Vector3d torque = new Vector3d();
            for (int i = 0; i < 4; i++) {
                Vector3d impulse = RcsOrientation.impulse(false, i, normal, 100, 0.05);
                Vector3d point = RcsOrientation.point(RcsOrientation.nozzle(false, i), normal);
                total.add(impulse);
                torque.add(point.sub(0.5, 0.5, 0.5).cross(impulse));
            }
            assertEquals(0, total.length(), 1e-12);
            assertEquals(0, torque.length(), 1e-12);
        }
    }

    @Test
    void perpendicularNozzlesKeepTheirFullThrust() {
        for (Direction normal : Direction.values()) {
            Vector3d first = RcsOrientation.impulse(false, 0, normal, 100, 0.05);
            Vector3d second = RcsOrientation.impulse(false, 2, normal, 100, 0.05);
            assertEquals(Math.sqrt(2) * 5.0, first.add(second).length(), 1e-12);
        }
    }

    @Test
    void configuredPnMatchesForcePerSecond() {
        Vector3d impulse = RcsOrientation.impulse(true, 0, Direction.UP, 100, 1);
        assertEquals(100, impulse.length(), 1e-12);
        assertEquals(0, RcsOrientation.impulse(true, 0, Direction.UP, 0, 1).length(), 1e-12);
    }

    @Test
    void offsetThrusterProducesTorque() {
        Vector3d impulse = RcsOrientation.impulse(true, 0, Direction.UP, 100, 0.05);
        Vector3d point = RcsOrientation.point(RcsOrientation.nozzle(true, 0), Direction.UP).add(2, 0, 0);
        Vector3d torque = point.sub(0.5, 0.5, 0.5).cross(impulse);
        assertEquals(10.0, Math.abs(torque.z), 1e-12);
    }
}
