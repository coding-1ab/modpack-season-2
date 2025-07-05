package com.kipti.bnb.content.nixie.large_nixie_tube;

import com.kipti.bnb.content.nixie.foundation.DoubleOrientedBlock;
import com.kipti.bnb.content.nixie.foundation.DoubleOrientedBlockModel;
import com.kipti.bnb.content.nixie.foundation.GenericNixieDisplayBlockEntity;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LargeNixieTubeBlock extends DoubleOrientedBlock implements IBE<GenericNixieDisplayBlockEntity>, IWrenchable {

    final @Nullable DyeColor dyeColor;

    public LargeNixieTubeBlock(Properties p_52591_, @Nullable DyeColor dyeColor) {
        super(p_52591_);
        this.dyeColor = dyeColor;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof DyeItem dyeItem && dyeItem.getDyeColor() != dyeColor) {
            if (!level.isClientSide) {
                DyeColor newColor = dyeItem.getDyeColor();
                BlockState newState = BnbBlocks.DYED_LARGE_NIXIE_TUBE.get(newColor).getDefaultState()
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(ORIENTATION, state.getValue(ORIENTATION))
                    .setValue(LIT, state.getValue(LIT));
                level.setBlockAndUpdate(pos, newState);
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

}
