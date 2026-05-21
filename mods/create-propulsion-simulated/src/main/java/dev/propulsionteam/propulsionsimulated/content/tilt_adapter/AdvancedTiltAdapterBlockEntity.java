package dev.propulsionteam.propulsionsimulated.content.tilt_adapter;

import java.util.List;

import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedTiltAdapterBlockEntity extends TiltAdapterBlockEntity {
    public static final int DEFAULT_ANGLE_LIMIT = 45;

    public AdvancedTiltAdapterAngleScrollBehaviour leftAngleBehaviour;
    public AdvancedTiltAdapterAngleScrollBehaviour rightAngleBehaviour;
    protected int leftAngleLimit = DEFAULT_ANGLE_LIMIT;
    protected int rightAngleLimit = DEFAULT_ANGLE_LIMIT;
    protected boolean sharedAngles = false;

    public AdvancedTiltAdapterBlockEntity(BlockPos pos, BlockState state) {
        super(PropulsionBlockEntities.ADVANCED_TILT_ADAPTER_BLOCK_ENTITY.get(), pos, state);
    }

    public AdvancedTiltAdapterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        leftAngleBehaviour = new AdvancedTiltAdapterAngleScrollBehaviour(
            Component.translatable("createpropulsion.advanced_tilt_adapter.left_angle"),
            this,
            true
        );
        leftAngleBehaviour.setStoredValue(leftAngleLimit);

        rightAngleBehaviour = new AdvancedTiltAdapterAngleScrollBehaviour(
            Component.translatable("createpropulsion.advanced_tilt_adapter.right_angle"),
            this,
            false
        );
        rightAngleBehaviour.setStoredValue(rightAngleLimit);

        behaviours.add(leftAngleBehaviour);
        behaviours.add(rightAngleBehaviour);
    }

    /** Called when a side's scroll value changes; keeps independent limits unless shared mode is on. */
    public void onAngleLimitChanged(boolean fromLeft, int value) {
        if (fromLeft) {
            leftAngleLimit = value;
            if (sharedAngles) {
                rightAngleLimit = value;
                if (rightAngleBehaviour != null) {
                    rightAngleBehaviour.setStoredValue(value);
                }
            }
        } else {
            rightAngleLimit = value;
            if (sharedAngles) {
                leftAngleLimit = value;
                if (leftAngleBehaviour != null) {
                    leftAngleBehaviour.setStoredValue(value);
                }
            }
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            targetAngle = clampToAngleLimits(computeTargetAngle());
            currentAngle = clampToAngleLimits(currentAngle);
            if (Math.abs(getTheoreticalSpeed()) > 0) {
                beginOrExtendKineticMove();
            } else {
                flickerTicker.scheduleUpdate(this::syncNetworkState);
            }
            sendData();
        }
    }

    public int getLeftAngleLimit() {
        return leftAngleLimit;
    }

    public int getRightAngleLimit() {
        return rightAngleLimit;
    }

    public boolean areAnglesShared() {
        return sharedAngles;
    }

    public void setSharedAngles(boolean shared) {
        if (sharedAngles == shared) {
            return;
        }
        sharedAngles = shared;
        if (shared && rightAngleBehaviour != null) {
            rightAngleBehaviour.setValue(leftAngleLimit);
        }
        setChanged();
        sendData();
    }

    @Override
    protected float getNeutralTargetAngle() {
        return -DEFAULT_ANGLE_LIMIT;
    }

    @Override
    protected float getPositiveSideAngleRange() {
        return leftAngleLimit;
    }

    @Override
    protected float getNegativeSideAngleRange() {
        return rightAngleLimit;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("LeftAngle", leftAngleLimit);
        compound.putInt("RightAngle", rightAngleLimit);
        compound.putBoolean("SharedAngles", sharedAngles);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        leftAngleLimit = compound.contains("LeftAngle") ? compound.getInt("LeftAngle") : DEFAULT_ANGLE_LIMIT;
        rightAngleLimit = compound.contains("RightAngle") ? compound.getInt("RightAngle") : DEFAULT_ANGLE_LIMIT;
        sharedAngles = compound.getBoolean("SharedAngles");
        if (leftAngleBehaviour != null) {
            leftAngleBehaviour.setStoredValue(leftAngleLimit);
        }
        if (rightAngleBehaviour != null) {
            rightAngleBehaviour.setStoredValue(rightAngleLimit);
        }
    }
}
