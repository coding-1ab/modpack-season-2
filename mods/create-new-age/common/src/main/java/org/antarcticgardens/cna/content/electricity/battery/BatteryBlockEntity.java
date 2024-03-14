package org.antarcticgardens.cna.content.electricity.battery;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.antarcticgardens.cna.CNABlockEntityTypes;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.energy.SimpleEnergyStorage;

import java.util.List;

public class BatteryBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer, IHaveGoggleInformation {
    public static final int MAX_SIZE = 3;
    public static final int SYNC_RATE = 8;

    private int size = 1;
    private int height = 1;

    private BlockPos controller = null;
    private BlockPos lastKnownPos = null;
    private boolean updateConnectivity = false;

    private int syncCooldown = 0;
    private boolean syncQueued = false;

    protected final SimpleEnergyStorage storage = new SimpleEnergyStorage(getBlockCapacity())
            .setMaxInsert(2000) // TODO: Config?
            .setMaxExtract(2000)
            .onFinalCommit(this::notifyUpdate);

    private SimpleEnergyStorage exposedStorage;

    protected final LerpedFloat gauge = LerpedFloat.linear();

    public BatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        EnergyStorage.registerForBlockEntity((be, dir) -> be.getEnergyStorage(),
                CNABlockEntityTypes.BATTERY.get());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();

        if (syncCooldown > 0) {
            if (--syncCooldown == 0 && syncQueued)
                sendData();
        }

        if (lastKnownPos == null) {
            lastKnownPos = worldPosition;
        } else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity)
            updateConnectivity();

        gauge.tickChaser();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
        if (level.isClientSide)
            invalidateRenderBoundingBox();
        else
            refreshExposed();
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
            tag.putLong("Capacity", storage.getCapacity());
            tag.putLong("Energy", storage.getStoredEnergy());
        } else {
            tag.put("Controller", NbtUtils.writeBlockPos(controller));
        }

        super.write(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = size;
        int prevHeight = height;

        updateConnectivity = tag.contains("UpdateConnectivity");

        if (tag.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(tag.getCompound("LastKnownPos"));

        if (tag.contains("Controller")) {
            controller = NbtUtils.readBlockPos(tag.getCompound("Controller"));
        } else {
            controller = null;
            size = tag.getInt("Size");
            height = tag.getInt("Height");
            storage.setCapacity(tag.getLong("Capacity"));
            storage.setStoredEnergy(tag.getLong("Energy"));

            if (storage.getCapacity() != 0) {
                double val = storage.getStoredEnergy() / (double) storage.getCapacity();
                gauge.chase(val, 0.125, LerpedFloat.Chaser.EXP);
            }
        }

        if (clientPacket) {
            boolean changeOfController = controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
            if (changeOfController || prevSize != size || prevHeight != height) {
                if (hasLevel())
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);

                invalidateRenderBoundingBox();
            }
        }
    }

    protected void updateConnectivity() {
        updateConnectivity = false;

        if (!level.isClientSide() && isController())
            ConnectivityHandler.formMulti(this);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BatteryBlockEntity controller = getControllerBE();
        if (controller == null)
            return false;

        SimpleEnergyStorage storage = controller.getEnergyStorage();

        Lang.translate("tooltip.create_new_age.energy_stats")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_storage", storage.getStoredEnergy(), storage.getCapacity())
                .style(ChatFormatting.AQUA).forGoggles(tooltip);

        return IHaveGoggleInformation.super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            syncQueued = true;
            return;
        }

        super.sendData();

        syncQueued = false;
        syncCooldown = SYNC_RATE;
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    @Override
    public void removeController(boolean keepContents) {
        if (!level.isClientSide()) {
            updateConnectivity = true;

            if (!keepContents)
                applySize(1);

            controller = null;
            size = 1;
            height = 1;

            BlockState state = getBlockState();
            if (BatteryBlock.isBattery(state)) {
                state = state.setValue(BatteryBlock.BOTTOM, true)
                        .setValue(BatteryBlock.TOP, true);
                getLevel().setBlock(worldPosition, state, 16 | 4 | 2 | 1);
            }

            long amount = storage.internalInsert(getEnergyStorage().internalExtract(getBlockCapacity(), true), true);
            storage.internalInsert(getEnergyStorage().internalExtract(amount, false), false);

            refreshExposed();
            setChanged();
            sendData();
        }
    }

    private void applySize(int size) {
        storage.setCapacity((long) size * getBlockCapacity());
    }

    private void refreshExposed() {
        if (isController()) {
            exposedStorage = storage;
        } else {
            exposedStorage = getControllerBE().storage;
        }
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    public BatteryBlockEntity getControllerBE() {
        if (isController())
            return this;
        else if (level != null && level.getBlockEntity(controller) instanceof BatteryBlockEntity be)
            return be;
        else
            return null;
    }

    @Override
    public boolean isController() {
        return controller == null || controller.equals(worldPosition);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(size - 1, height - 1, size - 1);
        else
            return super.createRenderBoundingBox();
    }

    @Override
    public void setController(BlockPos pos) {
        if (level.isClientSide() && !isVirtual())
            return;

        if (!pos.equals(controller)) {
            controller = pos;

            refreshExposed();
            setChanged();
            sendData();
        }
    }

    public SimpleEnergyStorage getEnergyStorage() {
        if (exposedStorage == null)
            refreshExposed();

        return exposedStorage;
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
        if (BatteryBlock.isBattery(state)) {
            state = state.setValue(BatteryBlock.BOTTOM, getController().getY() == getBlockPos().getY())
                    .setValue(BatteryBlock.TOP, getController().getY() + height - 1 == getBlockPos().getY());
            level.setBlock(worldPosition, state, 4 | 2);
        }

        if (isController()) {
            applySize(getTotalSize());

            for (int yOffset = 0; yOffset < height; yOffset++) {
                for (int xOffset = 0; xOffset < size; xOffset++) {
                    for (int zOffset = 0; zOffset < size; zOffset++) {
                        if (level.getBlockEntity(worldPosition.offset(xOffset, yOffset, zOffset)) instanceof BatteryBlockEntity be) {
                            long amount = storage.internalInsert(be.storage.internalExtract(storage.getCapacity(), true), true);
                            storage.internalInsert(be.storage.internalExtract(amount, false), false);
                            be.refreshExposed();
                        }
                    }
                }
            }
        }

        setChanged();
        sendData();
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

    public int getTotalSize() {
        return size * size * height;
    }

    public static int getMaxHeight() {
        return 32; // TODO: Config
    }

    public static int getBlockCapacity() {
        return 100000;
    }
}
