package dev.propulsionteam.propulsionsimulated.content.thruster.creative_vector_thruster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CreativeVectorThrusterValueBox extends ValueBoxTransform.Dual {
    public CreativeVectorThrusterValueBox(boolean first) {
        super(first);
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Vec3 local = VecHelper.voxelSpace(8f, isFirst() ? 14.5f : 1.5f, 2f);
        return rotatePointForFacing(local, state.getValue(CreativeVectorThrusterBlock.FACING));
    }

    @Override
    public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
        Direction side = state.getValue(CreativeVectorThrusterBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(side) + 180;
        float xRot = side == Direction.UP ? 90 : side == Direction.DOWN ? 270 : 0;
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xRot));
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
    }

    @Override
    public float getScale() {
        return 0.4975f;
    }

    private Vec3 rotatePointForFacing(Vec3 vec, Direction blockFacing) {
        return switch (blockFacing) {
            case NORTH -> vec;
            case EAST -> VecHelper.rotateCentered(vec, -90, Direction.Axis.Y);
            case SOUTH -> VecHelper.rotateCentered(vec, 180, Direction.Axis.Y);
            case WEST -> VecHelper.rotateCentered(vec, 90, Direction.Axis.Y);
            case UP -> VecHelper.rotateCentered(vec, 90, Direction.Axis.X);
            case DOWN -> VecHelper.rotateCentered(vec, -90, Direction.Axis.X);
        };
    }
}
