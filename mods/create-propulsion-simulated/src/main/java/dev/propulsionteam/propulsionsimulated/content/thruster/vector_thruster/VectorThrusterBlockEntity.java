package dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster;

import dev.propulsionteam.propulsionsimulated.content.thruster.IonThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.particles.ion.IonParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlock;
import java.util.List;

public class VectorThrusterBlockEntity extends IonThrusterBlockEntity {

    public LinkBehaviour leftLink;
    public LinkBehaviour rightLink;

    private int leftSignal;
    private int rightSignal;

    public VectorThrusterBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        
        leftLink = LinkBehaviour.receiver(this,
            ValueBoxTransform.Dual.makeSlots(isFirst -> new VectorThrusterLinkTransform(isFirst, false)),
            this::setLeftSignal);
            
        rightLink = LinkBehaviour.receiver(this,
            ValueBoxTransform.Dual.makeSlots(isFirst -> new VectorThrusterLinkTransform(isFirst, true)),
            this::setRightSignal);
            
        behaviours.add(leftLink);
        behaviours.add(rightLink);
    }

    private void setLeftSignal(int power) {
        this.leftSignal = power;
    }

    private void setRightSignal(int power) {
        this.rightSignal = power;
    }

    @Override
    protected void write(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("LeftSignal", leftSignal);
        compound.putInt("RightSignal", rightSignal);
    }

    @Override
    protected void read(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        leftSignal = compound.getInt("LeftSignal");
        rightSignal = compound.getInt("RightSignal");
    }

    @Override
    public double getNozzleOffsetFromCenter() {
        return 0.5;
    }

    @Override
    protected double getBaseThrust() { return Math.min(PropulsionConfig.VECTOR_THRUSTER_BASE_THRUST.get(), this.getRawThrustCap()); }

    @Override
    protected double getRawThrustCap() { return PropulsionConfig.VECTOR_THRUSTER_MAX_THRUST.get(); }

    @Override
    protected ParticleOptions createParticleOptions() {
        return new IonParticleData(List.of(), null, 0.85f);
    }

    private static class VectorThrusterLinkTransform extends ValueBoxTransform.Dual {
        private final boolean rightSide;

        public VectorThrusterLinkTransform(boolean first, boolean rightSide) {
            super(first);
            this.rightSide = rightSide;
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            double x = rightSide ? 6.5 / 16.0 : -6.5 / 16.0;
            double y = isFirst() ? 3.0 / 16.0 : -3.0 / 16.0;
            double z = -6.0 / 16.0;
            return new Vec3(x, y, z);
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            Direction facing = state.getValue(AbstractThrusterBlock.FACING);

            // rotate the whole front-face assembly to the block facing
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-facing.toYRot()));

            if (facing == Direction.UP) {
                ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
            } else if (facing == Direction.DOWN) {
                ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            }
        }

        @Override
        public float getScale() {
            return 0.25f;
        }
    }
}