package com.kipti.bnb.content.nixie.large_nixie_tube;

import com.kipti.bnb.content.nixie.foundation.*;
import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LargeNixieTubeBlock extends DoubleOrientedDisplayBlock implements IBE<GenericNixieDisplayBlockEntity>, IWrenchable, IGenericNixieDisplayBlock, DyeProviderBlock {

    final @Nullable DyeColor dyeColor;

    public LargeNixieTubeBlock(Properties p_52591_, @Nullable DyeColor dyeColor) {
        super(p_52591_);
        this.dyeColor = dyeColor;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return BnbBlocks.LARGE_NIXIE_TUBE.asItem().getDefaultInstance();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof DyeItem dyeItem && dyeItem.getDyeColor() != dyeColor) {
            if (!level.isClientSide) {
                GenericNixieDisplayBlockEntity be = (GenericNixieDisplayBlockEntity) level.getBlockEntity(pos);
                be.applyToEachElementOfThisStructure((display) -> {
                    DyeColor newColor = dyeItem.getDyeColor();
                    BlockState newState = BnbBlocks.DYED_LARGE_NIXIE_TUBE.get(newColor).getDefaultState()
                        .setValue(FACING, display.getBlockState().getValue(FACING))
                        .setValue(ORIENTATION, display.getBlockState().getValue(ORIENTATION))
                        .setValue(LIT, display.getBlockState().getValue(LIT));
                    level.setBlockAndUpdate(display.getBlockPos(), newState);
                    GenericNixieDisplayBlockEntity newBe = (GenericNixieDisplayBlockEntity) level.getBlockEntity(display.getBlockPos());
                    newBe.inheritDataFrom(display);
                });
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction frontTarget = DoubleOrientedBlockModel.getFront(state.getValue(FACING), state.getValue(ORIENTATION));
        boolean isFront = frontTarget.getAxis() == state.getValue(ORIENTATION).getAxis();
        return isFront ? BnbShapes.LARGE_NIXIE_TUBE_SIDE.get(state.getValue(FACING))
            : BnbShapes.LARGE_NIXIE_TUBE_FRONT.get(state.getValue(FACING));
    }

    @Override
    public Class<GenericNixieDisplayBlockEntity> getBlockEntityClass() {
        return GenericNixieDisplayBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GenericNixieDisplayBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GENERIC_NIXIE_DISPLAY.get();
    }

    public @Nullable DyeColor getDyeColor() {
        return dyeColor;
    }

    public List<GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions> getPossibleDisplayOptions() {
        return List.of(GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.NONE, GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.ALWAYS_UP);
    }

}
