package dev.propulsionteam.propulsionsimulated.content.thruster.rcs_thruster;

import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public final class RcsOrientation {
    private RcsOrientation() {}

    public static int xRotation(Direction normal) {
        return normal == Direction.UP ? 0 : normal == Direction.DOWN ? 180 : 90;
    }

    public static int yRotation(Direction normal) {
        return switch (normal) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    public static Quaternionf rotation(Direction normal) {
        return new Quaternionf().rotationY((float) Math.toRadians(-yRotation(normal)))
                .rotateX((float) Math.toRadians(-xRotation(normal)));
    }

    public static Vector3d direction(Vector3d v, Direction normal) {
        double x = v.x, y = v.y, z = v.z;
        return switch (normal) {
            case UP -> new Vector3d(x, y, z);
            case DOWN -> new Vector3d(x, -y, -z);
            case NORTH -> new Vector3d(x, z, -y);
            case EAST -> new Vector3d(y, z, x);
            case SOUTH -> new Vector3d(-x, z, y);
            case WEST -> new Vector3d(-y, z, -x);
        };
    }

    public static Vector3d point(Vector3d point, Direction normal) {
        return direction(new Vector3d(point).sub(0.5, 0.5, 0.5), normal).add(0.5, 0.5, 0.5);
    }

    public static Direction direction(Direction direction, Direction normal) {
        Vector3d v = direction(new Vector3d(direction.getStepX(), direction.getStepY(), direction.getStepZ()), normal);
        return Direction.getNearest(v.x, v.y, v.z);
    }

    public static Direction exhaust(boolean single, int nozzle) {
        if (single) return Direction.UP;
        return switch (nozzle) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.NORTH;
            case 2 -> Direction.WEST;
            default -> Direction.EAST;
        };
    }

    public static Vector3d nozzle(boolean single, int nozzle) {
        if (single) return new Vector3d(8, 14, 8).div(16);
        return switch (nozzle) {
            case 0 -> new Vector3d(8, 7, 16).div(16);
            case 1 -> new Vector3d(8, 7, 0).div(16);
            case 2 -> new Vector3d(0, 7, 8).div(16);
            default -> new Vector3d(16, 7, 8).div(16);
        };
    }

    public static Vector3d impulse(boolean single, int nozzle, Direction normal, double thrust, double timeStep) {
        Direction exhaust = direction(exhaust(single, nozzle), normal);
        return new Vector3d(exhaust.getStepX(), exhaust.getStepY(), exhaust.getStepZ())
                .mul(-thrust * timeStep);
    }
}
