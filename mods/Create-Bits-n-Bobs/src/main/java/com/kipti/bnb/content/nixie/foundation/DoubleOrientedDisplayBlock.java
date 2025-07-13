package com.kipti.bnb.content.nixie.foundation;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.nixie.nixie_board.NixieBoardBlock;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockItem;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Started off as a generic display block that can be oriented in two directions, but contains code for the displays in general.
 * */
public class DoubleOrientedDisplayBlock extends DirectionalBlock implements IWrenchable {

    public static final MapCodec<DoubleOrientedDisplayBlock> CODEC = simpleCodec(DoubleOrientedDisplayBlock::new);

    public static final DirectionProperty ORIENTATION = DirectionProperty.create("orientation");
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public DoubleOrientedDisplayBlock(Properties p_52591_) {
        super(p_52591_);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(ORIENTATION, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ORIENTATION, LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction surface = context.getClickedFace();
        Direction facing = context.getNearestLookingDirection();
        if (facing.getAxis() == surface.getAxis()) {
            facing = Arrays.stream(Direction.values())
                .filter(dir -> dir.getAxis() != surface.getAxis())

                .min(Comparator.comparingDouble(dir -> Vec3.atLowerCornerOf(dir.getNormal())
                    .distanceToSqr(Objects.requireNonNull(context.getPlayer()).getLookAngle())))

                .orElse(Direction.NORTH);
        }
        BlockState stateForPlacement = super.getStateForPlacement(context);
        return stateForPlacement == null ? null : stateForPlacement
            .setValue(FACING, surface)
            .setValue(ORIENTATION, facing.getOpposite());
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        GenericNixieDisplayBlockEntity be = (GenericNixieDisplayBlockEntity) context.getLevel().getBlockEntity(context.getClickedPos());
        GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions currentOption = be.getCurrentDisplayOption();
        List<GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions> options = be.getPossibleDisplayOptions();
        int currentIndex = options.indexOf(currentOption);
        if (currentIndex < 0) {
            CreateBitsnBobs.LOGGER.warn("No valid display option found for {}", be.getBlockPos());
            return InteractionResult.PASS;
        }
        int nextIndex = (currentIndex + 1) % options.size();
        GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions nextOption = options.get(nextIndex);
        be.applyToEachElementOfThisStructure((display) -> {
            display.setDisplayOption(nextOption);
        });
        //Purposefully don't allow default behaviour since that gives access to illegal states
        return InteractionResult.SUCCESS;
    }

    /*
     * Dont question it, this is how it works.
     * */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof NameTagItem && heldItem.has(DataComponents.CUSTOM_NAME)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GenericNixieDisplayBlockEntity display) {
                String name = heldItem.getHoverName().getString();
                display.findControllerBlockEntity().applyTextToDisplay(name, getLineForPlacement(state, hitResult.getBlockPos(), hitResult, level));
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (AllBlocks.CLIPBOARD.isIn(stack)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            List<ClipboardEntry> entries = ClipboardEntry.getLastViewedEntries(stack);
            int line = getLineForPlacement(state, hitResult.getBlockPos(), hitResult, level);
            for (ClipboardEntry entry : entries) {
                for (String string : entry.text.getString()
                    .split("\n")) {
                    if (blockEntity instanceof GenericNixieDisplayBlockEntity display) {
                        display.findControllerBlockEntity().applyTextToDisplay(string, line++);
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private int getLineForPlacement(BlockState state, BlockPos blockPos, BlockHitResult hitResult, Level level) {
        if (!(state.getBlock() instanceof NixieBoardBlock)) {
            return 0; // Nixie tubes always place on the first line
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof GenericNixieDisplayBlockEntity)) {
            return 0; // Not a Nixie display block entity
        }
        if (((GenericNixieDisplayBlockEntity) blockEntity).currentDisplayOption.lines == 1) {
            return 0; // Single line display, always place on the first line
        }

        Matrix4f rotation = DoubleOrientedBlockModel.getRotation(state.getValue(FACING), state.getValue(ORIENTATION));
        Vec3 globalPos = hitResult.getLocation().subtract(blockPos.getCenter());
        Vector4f localPos = rotation.invert().transform(new Vector4f(globalPos.toVector3f(), 1f));
        if (localPos.y < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

}
