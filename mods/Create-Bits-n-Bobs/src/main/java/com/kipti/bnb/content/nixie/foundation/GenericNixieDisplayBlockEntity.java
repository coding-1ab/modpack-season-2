package com.kipti.bnb.content.nixie.foundation;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.nixie.nixie_board.NixieBoardBlock;
import com.kipti.bnb.registry.BnbBlocks;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericNixieDisplayBlockEntity extends SmartBlockEntity {

    private static final Logger log = LoggerFactory.getLogger(GenericNixieDisplayBlockEntity.class);
    protected String currentTextTop = "";
    protected String currentTextBottom = "";
    protected ConfigurableDisplayOptions currentDisplayOption = ConfigurableDisplayOptions.NONE;

    public GenericNixieDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

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
        NONE(1, 1, () -> ConfigurableDisplayOptionTransform.NONE),
        ALWAYS_UP(1, 1, () -> ConfigurableDisplayOptionTransform.ALWAYS_UP),
        DOUBLE_CHAR(2, 1, () -> ConfigurableDisplayOptionTransform.DOUBLE_CHAR),
        DOUBLE_CHAR_DOUBLE_LINES(3, 2, () -> ConfigurableDisplayOptionTransform.DOUBLE_CHAR_DOUBLE_LINES);

        public final Supplier<ConfigurableDisplayOptionTransform> renderTransform;
        public final int width;
        public final int lines;

        ConfigurableDisplayOptions(int width, int lines, Supplier<ConfigurableDisplayOptionTransform> renderTransform) {
            this.renderTransform = renderTransform;
            this.width = width;
            this.lines = lines;
        }

    }

    public List<ConfigurableDisplayOptions> getPossibleDisplayOptions() {
        return ((IGenericNixieDisplayBlock) getBlockState().getBlock()).getPossibleDisplayOptions();
    }

    public void applyToEachElementOfThisStructure(Consumer<GenericNixieDisplayBlockEntity> consumer) {
        GenericNixieDisplayBlockEntity controller = findControllerBlockEntity();
        Direction facing = getBlockState().getValue(DoubleOrientedDisplayBlock.FACING);
        Direction orientation = getBlockState().getValue(DoubleOrientedDisplayBlock.ORIENTATION);
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
        //Apply clipping to the text
        EndClipping endClipping = getEndClipping();
        if (endClipping != EndClipping.NONE) {
            //Ensure any text inside the clipping region is space
            String topRaw = currentTextTop.trim();
            String bottomRaw = currentTextBottom.trim();
            int displayedCharacterWidth = calculateDisplayedCharacterWidth();
            currentTextTop = " ".repeat(endClipping.left) + (topRaw.isEmpty() ? "" : topRaw.substring(0, Math.min(displayedCharacterWidth, topRaw.length())));
            currentTextBottom = " ".repeat(endClipping.left) + (bottomRaw.isEmpty() ? "" : bottomRaw.substring(0, Math.min(displayedCharacterWidth, bottomRaw.length())));
        } else if (currentDisplayOption == ConfigurableDisplayOptions.NONE) {
            currentTextTop = currentTextTop.trim();
            currentTextBottom = currentTextBottom.trim();
        }
        notifyUpdate();
    }

    /*
     * Apparently, comprable isn't a word, i do not care, I will use it anyway.
     * */
    public static boolean areStatesComprableForConnection(BlockState state1, BlockState state2) {
        if (state1 == null || state2 == null) {
            return false;
        }

        final boolean stateOneIsBoard = BnbBlocks.NIXIE_BOARD.is(state1.getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(state1.getBlock());
        final boolean stateTwoIsBoard = BnbBlocks.NIXIE_BOARD.is(state2.getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(state2.getBlock());
        final boolean stateOneIsTube = BnbBlocks.LARGE_NIXIE_TUBE.is(state1.getBlock()) || BnbBlocks.DYED_LARGE_NIXIE_TUBE.contains(state1.getBlock());
        final boolean stateTwoIsTube = BnbBlocks.LARGE_NIXIE_TUBE.is(state2.getBlock()) || BnbBlocks.DYED_LARGE_NIXIE_TUBE.contains(state2.getBlock());

        if (!(stateOneIsBoard && stateTwoIsBoard) && !(stateOneIsTube && stateTwoIsTube)) return false;
        if (state1.getValue(DoubleOrientedDisplayBlock.FACING) != state2.getValue(DoubleOrientedDisplayBlock.FACING)) {
            return false;
        }
        return state1.getValue(DoubleOrientedDisplayBlock.ORIENTATION) == state2.getValue(DoubleOrientedDisplayBlock.ORIENTATION);
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
        Direction facing = getBlockState().getValue(DoubleOrientedDisplayBlock.FACING);
        Direction orientation = getBlockState().getValue(DoubleOrientedDisplayBlock.ORIENTATION);
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
        EndClipping endClipping = getEndClipping();
        int consumptionWidth = calculateDisplayedCharacterWidth();
        String consumed = " ".repeat(endClipping.left) + (tagElement.isEmpty() ? "" : tagElement.substring(0, Math.min(consumptionWidth, tagElement.length())));
        if (line == 0) {
            this.currentTextTop = consumed;
        } else if (line == 1) {
            this.currentTextBottom = consumed;
        } else {
            return;
        }
        Direction right = DoubleOrientedBlockModel.getLeft(
                getBlockState().getValue(DoubleOrientedDisplayBlock.FACING),
                getBlockState().getValue(DoubleOrientedDisplayBlock.ORIENTATION)
        ).getOpposite();
        BlockPos rightPos = getBlockPos().relative(right);
        BlockEntity blockEntity = level.getBlockEntity(rightPos);
        if (blockEntity instanceof GenericNixieDisplayBlockEntity nextDisplay && areStatesComprableForConnection(getBlockState(), nextDisplay.getBlockState())) {
            nextDisplay.currentDisplayOption = currentDisplayOption;
            nextDisplay.applyTextToDisplay(tagElement.isEmpty() ? "" : tagElement.substring(Math.min(consumptionWidth, tagElement.length())), line);
        }
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(DoubleOrientedDisplayBlock.LIT,
                !this.currentTextTop.trim().isEmpty() && (currentDisplayOption.lines == 1 || !this.currentTextTop.trim().isEmpty())));
        notifyUpdate();
    }

    public int seekWidth() {
        Direction right = DoubleOrientedBlockModel.getLeft(
                getBlockState().getValue(DoubleOrientedDisplayBlock.FACING),
                getBlockState().getValue(DoubleOrientedDisplayBlock.ORIENTATION)
        ).getOpposite();
        int characterCount = 0;
        for (int i = 0; i < 100; i++) {
            BlockPos nextPos = getBlockPos().relative(right, i);
            BlockEntity blockEntity = level.getBlockEntity(nextPos);
            if (!areStatesComprableForConnection(getBlockState(), level.getBlockState(nextPos))) {
                break;
            }
            if (blockEntity instanceof GenericNixieDisplayBlockEntity currentWalkNixieDisplay) {
                characterCount += currentWalkNixieDisplay.calculateDisplayedCharacterWidth();
            } else {
                CreateBitsnBobs.LOGGER.warn("Found unexpected non-nixie display block entity at {} while seeking width for {}", nextPos, getBlockPos());
                break;
            }
        }
        return characterCount;
    }

    int calculateDisplayedCharacterWidth() {
        EndClipping endClipping = getEndClipping();
        return currentDisplayOption.width - endClipping.left - endClipping.right;
    }

    public enum EndClipping {
        NONE(0, 0),
        LEFT(1, 0),
        RIGHT(0, 1),
        BOTH(1, 1);

        public final int left;
        public final int right;

        EndClipping(int left, int right) {
            this.left = left;
            this.right = right;
        }
    }

    public EndClipping getEndClipping() {
        if (!(getBlockState().getBlock() instanceof NixieBoardBlock) || currentDisplayOption != ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES) {
            return EndClipping.NONE; // Nixie tubes do not have end clipping
        }
        boolean left = !getBlockState().getValue(NixieBoardBlock.LEFT);
        boolean right = !getBlockState().getValue(NixieBoardBlock.RIGHT);
        if (left && right) {
            return EndClipping.BOTH;
        } else if (left) {
            return EndClipping.LEFT;
        } else if (right) {
            return EndClipping.RIGHT;
        }
        return EndClipping.NONE;
    }

}
