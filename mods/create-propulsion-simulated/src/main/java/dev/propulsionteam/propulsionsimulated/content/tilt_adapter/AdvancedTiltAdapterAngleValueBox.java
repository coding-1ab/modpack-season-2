package dev.propulsionteam.propulsionsimulated.content.tilt_adapter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Value box centered on a tilt adapter redstone face; offset rotates with block orientation. */
public class AdvancedTiltAdapterAngleValueBox extends ValueBoxTransform {
    private static final Vec3 FACE_CENTER_SOUTH = VecHelper.voxelSpace(8f, 8f, 15.5f);

    /** {@code true} = left redstone face (positive differential input). */
    private final boolean controlsLeftLimit;

    public AdvancedTiltAdapterAngleValueBox(boolean controlsLeftLimit) {
        this.controlsLeftLimit = controlsLeftLimit;
    }

    private Direction getSide(BlockState state) {
        return AbstractTiltAdapterBlock.getRedstoneSide(state, controlsLeftLimit);
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction side = getSide(state);
        Vec3 location = FACE_CENTER_SOUTH;
        location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(side), Axis.Y);
        location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(side), Axis.X);
        return location;
    }

    @Override
    public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
        Direction side = getSide(state);
        float yRot = AngleHelper.horizontalAngle(side) + 180;
        float xRot = side == Direction.UP ? 90 : side == Direction.DOWN ? 270 : 0;
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xRot));
    }

    @Override
    public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
        Direction expectedFace = getSide(state);
        if (dominantFace(localHit) != expectedFace) {
            return false;
        }
        Vec3 offset = getLocalOffset(level, pos, state);
        return localHit.distanceTo(offset) < getScale() / 3.5f;
    }

    /** Which block face the click is on (local hit is 0–1 inside the block). */
    private static Direction dominantFace(Vec3 localHit) {
        Direction best = Direction.UP;
        double bestDist = Double.MAX_VALUE;
        for (Direction face : Direction.values()) {
            double dist = distanceToFacePlane(localHit, face);
            if (dist < bestDist) {
                bestDist = dist;
                best = face;
            }
        }
        return best;
    }

    private static double distanceToFacePlane(Vec3 local, Direction face) {
        return switch (face) {
            case WEST -> local.x;
            case EAST -> 1.0 - local.x;
            case DOWN -> local.y;
            case UP -> 1.0 - local.y;
            case NORTH -> local.z;
            case SOUTH -> 1.0 - local.z;
        };
    }
}
