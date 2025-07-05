package com.kipti.bnb.content.nixie.foundation;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class GenericNixieDisplayBlockEntity extends SmartBlockEntity {

    private static final Logger log = LoggerFactory.getLogger(GenericNixieDisplayBlockEntity.class);
    protected char currentChar = ' ';

    public GenericNixieDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("currentText", currentChar);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        currentChar = (char) tag.getInt("currentText");
    }

    public int getCurrentChar() {
        return currentChar;
    }

    public GenericNixieDisplayBlockEntity findControllerBlockEntity() {
        Direction facing = getBlockState().getValue(DoubleOrientedBlock.FACING);
        Direction orientation = getBlockState().getValue(DoubleOrientedBlock.ORIENTATION);
        Direction left = DoubleOrientedBlockModel.getLeft(facing, orientation);
        BlockPos leftPos = getBlockPos().relative(left);
        GenericNixieDisplayBlockEntity lastDisplay = this;
        for (int i = 0; i < 100; i++) {
            BlockEntity blockEntity = level.getBlockEntity(leftPos);
            if (blockEntity instanceof GenericNixieDisplayBlockEntity display) {
                if (display.getBlockState().is(this.getBlockState().getBlock())) {
                    lastDisplay = display;
                }
            } else {
                break; // No more display found
            }
            leftPos = leftPos.relative(left);
        }
        return lastDisplay;
    }

    public void applyTextToDisplay(String tagElement) {
        this.currentChar = tagElement.isEmpty() ? ' ' : tagElement.charAt(0);
        Direction right = DoubleOrientedBlockModel.getLeft(
            getBlockState().getValue(DoubleOrientedBlock.FACING),
            getBlockState().getValue(DoubleOrientedBlock.ORIENTATION)
        ).getOpposite();
        BlockPos rightPos = getBlockPos().relative(right);
        BlockEntity blockEntity = level.getBlockEntity(rightPos);
        if (blockEntity instanceof GenericNixieDisplayBlockEntity nextDisplay) {
            nextDisplay.applyTextToDisplay(tagElement.isEmpty() ? "" : tagElement.substring(1));
        }
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(DoubleOrientedBlock.LIT, currentChar != ' '));
        notifyUpdate();
    }

    public int seekWidth() {
        Direction right = DoubleOrientedBlockModel.getLeft(
            getBlockState().getValue(DoubleOrientedBlock.FACING),
            getBlockState().getValue(DoubleOrientedBlock.ORIENTATION)
        ).getOpposite();
        for (int i = 0; i < 100; i++) {
            BlockPos nextPos = getBlockPos().relative(right, i);
            BlockEntity blockEntity = level.getBlockEntity(nextPos);
            if (!(blockEntity instanceof GenericNixieDisplayBlockEntity display) || !display.getBlockState().is(this.getBlockState().getBlock())) {
                return i + 1;
            }
        }
        return 1; // Default width if no other displays found
    }
}
