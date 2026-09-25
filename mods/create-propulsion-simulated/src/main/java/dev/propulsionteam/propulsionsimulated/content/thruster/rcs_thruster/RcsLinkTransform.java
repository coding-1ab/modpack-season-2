package dev.propulsionteam.propulsionsimulated.content.thruster.rcs_thruster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class RcsLinkTransform extends ValueBoxTransform.Dual {
    private final int nozzle;

    public RcsLinkTransform(boolean first, int nozzle) {
        super(first);
        this.nozzle = nozzle;
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        boolean single = ((RcsThrusterBlock) state.getBlock()).isSingle();
        Vector3d point;
        if (single) point = new Vector3d(isFirst() ? 5 : 11, 7, 3.95);
        else point = switch (nozzle) {
            case 0 -> new Vector3d(isFirst() ? 5 : 11, 2, 15.05);
            case 1 -> new Vector3d(isFirst() ? 11 : 5, 2, 0.95);
            case 2 -> new Vector3d(0.95, 2, isFirst() ? 5 : 11);
            default -> new Vector3d(15.05, 2, isFirst() ? 6 : 11);
        };
        point = RcsOrientation.point(point.div(16), RcsThrusterBlock.normal(state));
        return new Vec3(point.x, point.y, point.z);
    }

    @Override
    public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
        boolean single = ((RcsThrusterBlock) state.getBlock()).isSingle();
        Direction side = single ? Direction.NORTH : RcsOrientation.exhaust(false, nozzle);
        ms.mulPose(RcsOrientation.rotation(RcsThrusterBlock.normal(state)));
        ms.mulPose(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(side) + 180));
    }

    @Override
    public void transform(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
        super.transform(level, pos, state, ms);
        ms.scale(0.75f, 0.75f, 0.75f);
    }

    @Override
    public float getScale() { return 0.4975f; }
}
