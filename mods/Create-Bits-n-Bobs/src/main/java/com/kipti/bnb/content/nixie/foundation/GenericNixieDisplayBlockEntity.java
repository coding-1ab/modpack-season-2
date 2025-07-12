package com.kipti.bnb.content.nixie.foundation;

import com.kipti.bnb.registry.BnbBlocks;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class GenericNixieDisplayBlockEntity extends SmartBlockEntity {

    private static final Logger log = LoggerFactory.getLogger(GenericNixieDisplayBlockEntity.class);
    protected String currentTextTop = "";
    protected String currentTextBottom = "";
    protected ConfigurableDisplayOptions currentDisplayOption = ConfigurableDisplayOptions.NONE;

    public GenericNixieDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

//    @Override
//    public void tick() {
//        super.tick();
//
//        Direction leftDir = DoubleOrientedBlockModel.getLeft(
//            getBlockState().getValue(DoubleOrientedBlock.FACING),
//            getBlockState().getValue(DoubleOrientedBlock.ORIENTATION)
//        );
//
//        drawArrow(leftDir, 0xff0000);
//        drawArrow(getBlockState().getValue(DoubleOrientedBlock.FACING), 0x00ff00);
//        drawArrow(getBlockState().getValue(DoubleOrientedBlock.ORIENTATION), 0x0000ff);
//    }

//    private void drawArrow(Direction leftDir, int i) {
//        Vec3 left = Vec3.atLowerCornerOf(leftDir.getNormal());
//        Vec3 up = new Vec3(0, 1, 0).cross(left);
//        if (up.lengthSqr() < 0.01) {
//            up = new Vec3(0, 0, 1); // Fallback to Z axis if no up direction is found
//        }
//
//        //Render an arrow for outliner
//        Outliner.getInstance().showLine(hashCode() + "_" + leftDir + "_arrow_straight", getBlockPos().getCenter().add(left), getBlockPos().getCenter()).colored(i);
//        Outliner.getInstance().showLine(hashCode() + "_" + leftDir + "_arrow_up", getBlockPos().getCenter().add(left), getBlockPos().getCenter().add(left.scale(0.75)).add(up.scale(0.5))).colored(i);
//        Outliner.getInstance().showLine(hashCode() + "_" + leftDir + "_arrow_down", getBlockPos().getCenter().add(left), getBlockPos().getCenter().add(left.scale(0.75)).subtract(up.scale(0.5))).colored(i);
//    }

    public void inheritDataFrom(GenericNixieDisplayBlockEntity be) {
        this.currentTextTop = be.currentTextTop;
        this.currentTextBottom = be.currentTextBottom;
        this.currentDisplayOption = be.currentDisplayOption;
        notifyUpdate();
    }

    public ConfigurableDisplayOptions getCurrentDisplayOption() {
        return currentDisplayOption;
    }

    public enum ConfigurableDisplayOptions {
        NONE,
        ALWAYS_UP,
        DOUBLE_CHAR,
        DOUBLE_CHAR_DOUBLE_LINES
    }

    public List<ConfigurableDisplayOptions> getPossibleDisplayOptions() {
        return ((IGenericNixieDisplayBlock) getBlockState().getBlock()).getPossibleDisplayOptions();
    }

    public void applyToEachElementOfThisStructure(Consumer<GenericNixieDisplayBlockEntity> consumer) {
        GenericNixieDisplayBlockEntity controller = findControllerBlockEntity();
        Direction facing = getBlockState().getValue(DoubleOrientedBlock.FACING);
        Direction orientation = getBlockState().getValue(DoubleOrientedBlock.ORIENTATION);
        Direction right = DoubleOrientedBlockModel.getLeft(facing, orientation).getOpposite();
        BlockPos currentPos = controller.getBlockPos();
        for (int i = 0; i < 100; i++) {
            BlockEntity blockEntity = controller.level.getBlockEntity(currentPos);
            if (blockEntity instanceof GenericNixieDisplayBlockEntity display && areStatesComprableForConnection(controller.getBlockState(), display.getBlockState())) {
                consumer.accept(display);
            } else {
                break; // No more display found
            }
            currentPos = currentPos.relative(right);
        }
    }

    public void setDisplayOption(ConfigurableDisplayOptions option) {
        if (currentDisplayOption == option) {
            return; // No change
        }
        currentDisplayOption = option;
//        this.currentTextTop = "";
//        this.currentTextBottom = "";
        notifyUpdate();
    }

    /*
     * Apparently, comprable isn't a word, i do not care, I will use it anyway.
     * */
    public static boolean areStatesComprableForConnection(BlockState state1, BlockState state2) {
        if (state1 == null || state2 == null) {
            return false;
        }

        boolean stateOneIsBoard = BnbBlocks.NIXIE_BOARD.is(state1.getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(state1.getBlock());
        boolean stateTwoIsBoard = BnbBlocks.NIXIE_BOARD.is(state2.getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(state2.getBlock());
        boolean stateOneIsTube = BnbBlocks.LARGE_NIXIE_TUBE.is(state1.getBlock()) || BnbBlocks.DYED_LARGE_NIXIE_TUBE.contains(state1.getBlock());
        boolean stateTwoIsTube = BnbBlocks.LARGE_NIXIE_TUBE.is(state2.getBlock()) || BnbBlocks.DYED_LARGE_NIXIE_TUBE.contains(state2.getBlock());

        if (!(stateOneIsBoard && stateTwoIsBoard) && !(stateOneIsTube && stateTwoIsTube)) return false;
        if (state1.getValue(DoubleOrientedBlock.FACING) != state2.getValue(DoubleOrientedBlock.FACING)) {
            return false;
        }
        return state1.getValue(DoubleOrientedBlock.ORIENTATION) == state2.getValue(DoubleOrientedBlock.ORIENTATION);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("CurrentTextTop", currentTextTop);
        tag.putString("CurrentTextBottom", currentTextBottom);
        tag.putString("CurrentDisplayOption", currentDisplayOption.name());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        currentTextTop = tag.getString("CurrentTextTop");
        currentTextBottom = tag.getString("CurrentTextBottom");
        String optionName = tag.getString("CurrentDisplayOption");
        try {
            currentDisplayOption = ConfigurableDisplayOptions.valueOf(optionName);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid display option '{}' for block entity at {}. Defaulting to NONE.", optionName, getBlockPos());
            currentDisplayOption = ConfigurableDisplayOptions.NONE;
        }
    }

    public @NotNull GenericNixieDisplayBlockEntity findControllerBlockEntity() {
        Direction facing = getBlockState().getValue(DoubleOrientedBlock.FACING);
        Direction orientation = getBlockState().getValue(DoubleOrientedBlock.ORIENTATION);
        Direction left = DoubleOrientedBlockModel.getLeft(facing, orientation);
        BlockPos leftPos = getBlockPos().relative(left);
        GenericNixieDisplayBlockEntity lastDisplay = this;
        for (int i = 0; i < 100; i++) {
            BlockEntity blockEntity = level.getBlockEntity(leftPos);
            if (blockEntity instanceof GenericNixieDisplayBlockEntity display && areStatesComprableForConnection(getBlockState(), display.getBlockState())) {
                lastDisplay = display;
            } else {
                break; // No more display found
            }
            leftPos = leftPos.relative(left);
        }
        return lastDisplay;
    }

    public void applyTextToDisplay(String tagElement, int line) {
        boolean isDoubleLines = currentDisplayOption == ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES;
        int consumptionWidth = currentDisplayOption == ConfigurableDisplayOptions.DOUBLE_CHAR ||
            isDoubleLines ? 2 : 1;
        String consumed = tagElement.isEmpty() ? "" : tagElement.substring(0, Math.min(consumptionWidth, tagElement.length()));
        if (line == 0) {
            this.currentTextTop = consumed;
        } else if (line == 1) {
            this.currentTextBottom = consumed;
        } else {
            return;
        }
        Direction right = DoubleOrientedBlockModel.getLeft(
            getBlockState().getValue(DoubleOrientedBlock.FACING),
            getBlockState().getValue(DoubleOrientedBlock.ORIENTATION)
        ).getOpposite();
        BlockPos rightPos = getBlockPos().relative(right);
        BlockEntity blockEntity = level.getBlockEntity(rightPos);
        if (blockEntity instanceof GenericNixieDisplayBlockEntity nextDisplay && areStatesComprableForConnection(getBlockState(), nextDisplay.getBlockState())) {
            nextDisplay.currentDisplayOption = currentDisplayOption;
            nextDisplay.applyTextToDisplay(tagElement.isEmpty() ? "" : tagElement.substring(Math.min(consumptionWidth, tagElement.length())), line);
        }
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(DoubleOrientedBlock.LIT,
            !this.currentTextTop.trim().isEmpty() && (!isDoubleLines || !this.currentTextTop.trim().isEmpty())));
        notifyUpdate();
    }

    public int seekWidth() {
        Direction right = DoubleOrientedBlockModel.getLeft(
            getBlockState().getValue(DoubleOrientedBlock.FACING),
            getBlockState().getValue(DoubleOrientedBlock.ORIENTATION)
        ).getOpposite();
        int characterCount = currentDisplayOption == ConfigurableDisplayOptions.DOUBLE_CHAR ||
            currentDisplayOption == ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES ? 2 : 1;
        for (int i = 0; i < 100; i++) {
            BlockPos nextPos = getBlockPos().relative(right, i);
            if (!areStatesComprableForConnection(getBlockState(), level.getBlockState(nextPos))) {
                return i * characterCount; // Return the width based on the number of connected displays
            }
        }
        return 1; // Default width if no other displays found
    }
}
