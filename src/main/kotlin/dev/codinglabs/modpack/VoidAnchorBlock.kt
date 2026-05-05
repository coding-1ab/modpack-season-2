package dev.codinglabs.modpack

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.core.Direction
import net.minecraft.world.phys.BlockHitResult

class VoidAnchorBlock(
    properties: Properties
): BaseEntityBlock(properties), EntityBlock {

    companion object {
        val CODEC: MapCodec<VoidAnchorBlock> = simpleCodec(::VoidAnchorBlock)
        val CHARGES: IntegerProperty = IntegerProperty.create("charges", 0, 4)
        val ACTIVE: BooleanProperty = BooleanProperty.create("active")
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING

    }

    val noPortalKey get() = MSG_NO_PORTAL

    init {
        registerDefaultState(stateDefinition.any().setValue(CHARGES, 0).setValue(ACTIVE, false).setValue(FACING, Direction.NORTH))
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide && player is ServerPlayer) {
            player.voidAnchorPos = pos
            VoidAnchorLogic.refreshActivation(level, pos, state)
        }
        return InteractionResult.SUCCESS
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult,
    ): ItemInteractionResult {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }

        if (!level.isClientSide && player is ServerPlayer) {
            player.voidAnchorPos = pos
            val refreshed = VoidAnchorLogic.refreshActivation(level, pos, state)
            if (!refreshed.getValue(ACTIVE)) {
                this.name
                player.displayClientMessage(Component.translatable(noPortalKey), true)
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
            VoidAnchorLogic.refreshActivation(level, pos, state)
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
