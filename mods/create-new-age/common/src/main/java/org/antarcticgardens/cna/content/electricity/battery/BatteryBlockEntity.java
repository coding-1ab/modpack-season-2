package org.antarcticgardens.cna.content.electricity.battery;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BatteryBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer {
    public static int MAX_SIZE = 3;

    private int size = 1;
    private int height = 1;

    private BlockPos controller = null;
    private BlockPos lastKnownPos = null;
    private boolean updateConnectivity = false;

    public BatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void removeController(boolean keepContents) {
        if (!level.isClientSide()) {
            updateConnectivity = true;

            controller = null;
            size = 1;
            height = 1;

            BlockState state = getBlockState();
            if (state.getBlock() instanceof BatteryBlock) {
                state = state.setValue(BatteryBlock.BOTTOM, true)
                        .setValue(BatteryBlock.TOP, true);
                getLevel().setBlock(worldPosition, state, 16 | 4 | 2 | 1);
            }

            setChanged();
            sendData();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (lastKnownPos == null) {
            lastKnownPos = worldPosition;
        } else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
            onPositionChanged();
            lastKnownPos = worldPosition;
        }

        if (updateConnectivity)
            updateConnectivity();
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        if (updateConnectivity)
            tag.putBoolean("UpdateConnectivity", true);

        if (lastKnownPos != null)
            tag.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));

        if (isController()) {
            tag.putInt("Size", size);
            tag.putInt("Height", height);
        } else {
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
        }

        super.write(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        updateConnectivity = tag.contains("UpdateConnectivity");

        if (tag.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(tag.getCompound("LastKnownPos"));

        if (tag.contains("Controller")) {
            controller = NbtUtils.readBlockPos(tag.getCompound("Controller"));
        } else {
            size = tag.getInt("Size");
            height = tag.getInt("Height");
        }
    }

    protected void updateConnectivity() {
        updateConnectivity = false;

        if (!level.isClientSide() && isController())
            ConnectivityHandler.formMulti(this);
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    public BatteryBlockEntity getControllerBE() {
        if (isController())
            return this;
        else if (level != null && level.getBlockEntity(getController()) instanceof BatteryBlockEntity be)
            return be;
        else
            return null;
    }

    @Override
    public boolean isController() {
        return controller == null || controller.equals(worldPosition);
    }

    @Override
    public void setController(BlockPos pos) {
        if (!pos.equals(controller) && !pos.equals(worldPosition)) {
            controller = pos;

            setChanged();
            sendData();
        }
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof BatteryBlock) {
            state.setValue(BatteryBlock.BOTTOM, getController().getY() == getBlockPos().getY())
                    .setValue(BatteryBlock.TOP, getController().getY() + height - 1 == getBlockPos().getY());
            level.setBlock(worldPosition, state, 4 | 2);
        }

        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return getMaxHeight();

        return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return MAX_SIZE;
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public void setWidth(int width) {
        this.size = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    public static int getMaxHeight() {
        return 32; // TODO: Config
    }
}
