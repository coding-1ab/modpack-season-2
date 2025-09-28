package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GirderStrutBlockItem extends BlockItem {

    public GirderStrutBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //Check if the current stack has the place from data tag, if not, add it in

        ItemStack stack = context.getItemInHand();

        if (stack.has(BnbDataComponents.GIRDER_STRUT_FROM)) {
            return super.useOn(context);
        }

        BlockPos clickedPos = context.getClickedPos();

        //If clicked pos is not already a girder strut block, offset by the clicked face
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);
        if (!(clickedState.getBlock() instanceof GirderStrutBlock)) {
            clickedPos = clickedPos.relative(context.getClickedFace());
        }

        stack.set(BnbDataComponents.GIRDER_STRUT_FROM, clickedPos);

        return InteractionResult.SUCCESS;
    }
}
