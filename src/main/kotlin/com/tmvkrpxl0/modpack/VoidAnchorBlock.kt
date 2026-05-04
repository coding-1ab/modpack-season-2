package com.tmvkrpxl0.modpack

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.BlockHitResult

class VoidAnchorBlock(
    properties: Properties
): BaseEntityBlock(properties), EntityBlock {

    companion object {
        val CODEC: MapCodec<VoidAnchorBlock> = simpleCodec(::VoidAnchorBlock)
        val CHARGES: IntegerProperty = IntegerProperty.create("charges", 0, 4)
        val ACTIVE: BooleanProperty = BooleanProperty.create("active")
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(CHARGES, 0).setValue(ACTIVE, false))
    }

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide && player is ServerPlayer) {
            VoidAnchorLogic.saveLastClickedAnchor(player, pos)
            VoidAnchorLogic.refreshActivation(level, pos)
        }
        return InteractionResult.SUCCESS
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: net.minecraft.world.InteractionHand,
        hitResult: BlockHitResult,
    ): ItemInteractionResult {
        if (hand != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }

        if (!level.isClientSide && player is ServerPlayer) {
            VoidAnchorLogic.saveLastClickedAnchor(player, pos)
            val refreshed = VoidAnchorLogic.refreshActivation(level, pos)
            if (!refreshed.getValue(ACTIVE)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("공허 정박기 비활성화: 엔드 포탈 25칸 이내에서만 동작"), true)
                return ItemInteractionResult.CONSUME
            }
        }

        if (!stack.`is`(Items.ENDER_PEARL)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }

        val currentCharges = state.getValue(CHARGES)
        if (currentCharges >= 4) {
            return ItemInteractionResult.CONSUME
        }

        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(CHARGES, currentCharges + 1), 3)
            if (!player.isCreative) {
                stack.shrink(1)
            }
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f)
        }
        return ItemInteractionResult.SUCCESS
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, movedByPiston: Boolean) {
        super.onPlace(state, level, pos, oldState, movedByPiston)
        if (!level.isClientSide) {
            VoidAnchorLogic.refreshActivation(level, pos)
        }
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return VoidAnchorBlockEntity(pos, state)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(CHARGES, ACTIVE)
    }

    override fun codec(): MapCodec<VoidAnchorBlock> {
        return CODEC
    }

}
