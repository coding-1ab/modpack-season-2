package com.kipti.bnb.content.cogwheel_chain.item;

import com.kipti.bnb.content.cogwheel_chain.graph.PartialCogwheelChain;
import com.kipti.bnb.registry.BnbDataComponents;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class CogwheelChainItem extends Item {

    public CogwheelChainItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //Check if there's an existing PartialCogwheelChain in this item
        ItemStack stack = context.getItemInHand();
        BlockState clickedBlockState = context.getLevel().getBlockState(context.getClickedPos());

        if (stack.has(BnbDataComponents.PARTIAL_COGWHEEL_CHAIN)) {
            if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
                //If the player is crouching, remove the existing chain
                if (!context.getLevel().isClientSide) stack.remove(BnbDataComponents.PARTIAL_COGWHEEL_CHAIN);
                return InteractionResult.SUCCESS;
            }

            PartialCogwheelChain chain = stack.get(BnbDataComponents.PARTIAL_COGWHEEL_CHAIN);
            boolean added;
            try {
                assert chain != null;
                added = chain.tryAddNode(context.getLevel(), context.getClickedPos(), clickedBlockState);
            } catch (PartialCogwheelChain.ChainAdditionAbortedException e) {
                context.getPlayer().displayClientMessage(Component.literal(e.getMessage()).withColor(0xff0000), true);
                return InteractionResult.FAIL;
            }

            boolean completed = chain.completeIfLooping(context.getLevel());
            if (completed) {//TODO: cost chains or something
                if (!context.getLevel().isClientSide) stack.remove(BnbDataComponents.PARTIAL_COGWHEEL_CHAIN);
            }

            return added ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        //If it is a valid target, create a new PartialCogwheelChain and store it in the item
        if (PartialCogwheelChain.isValidBlockTarget(context.getLevel(), context.getClickedPos(), clickedBlockState) && clickedBlockState.getBlock() instanceof ICogWheel cogWheel) {
            if (!context.getLevel().isClientSide) {
                PartialCogwheelChain chain = new PartialCogwheelChain(context.getClickedPos(), clickedBlockState.getValue(CogWheelBlock.AXIS), cogWheel.isLargeCog());
                stack.set(BnbDataComponents.PARTIAL_COGWHEEL_CHAIN, chain);
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

}
