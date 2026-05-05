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

        val MSG_NO_PORTAL    = "block.modpack_tweaks.void_anchor.no_portal"
        val MSG_SET          = "block.modpack_tweaks.void_anchor.set"
        val MSG_FULL         = "block.modpack_tweaks.void_anchor.full"
        val MSG_CHARGES      = "block.modpack_tweaks.void_anchor.charges"
        val MSG_DESTROYED    = "block.modpack_tweaks.void_anchor.destroyed"
        val MSG_NO_CHARGES   = "block.modpack_tweaks.void_anchor.no_charges"
    }

    val noPortalKey get() = MSG_NO_PORTAL

    init {
        registerDefaultState(stateDefinition.any().setValue(CHARGES, 0).setValue(ACTIVE, false).setValue(FACING, Direction.NORTH))
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide && player is ServerPlayer) {
            val refreshed = VoidAnchorLogic.refreshActivation(level, pos, state)
            if (!refreshed.getValue(ACTIVE)) {
                player.displayClientMessage(Component.translatable(MSG_NO_PORTAL), true)
                return InteractionResult.sidedSuccess(level.isClientSide)
            }

            // 이미 같은곳에 등록되어있다면 생략
            if (player.voidAnchorPos != null && player.voidAnchorPos == pos) return InteractionResult.sidedSuccess(level.isClientSide)

            player.voidAnchorPos = pos
            val charges = refreshed.getValue(CHARGES)
            player.displayClientMessage(
                Component.translatable(MSG_SET)
                    .append(" ")
                    .append(Component.translatable(MSG_CHARGES, charges, 4)),
                true
            )
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f)
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
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

        // 엔더 진주가 아니면 빈 손 상호작용(useWithoutItem)으로 넘기긱
        if (!stack.`is`(Items.ENDER_PEARL)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }

        // 충전
        val currentCharges = state.getValue(CHARGES)
        if (!level.isClientSide && player is ServerPlayer) {
            if (currentCharges >= 4) {
                player.displayClientMessage(Component.translatable(MSG_FULL), true)
                return ItemInteractionResult.CONSUME
            }
            val refreshed = VoidAnchorLogic.refreshActivation(level, pos, state)
            if (!refreshed.getValue(ACTIVE)) {
                player.displayClientMessage(Component.translatable(MSG_NO_PORTAL), true)
                return ItemInteractionResult.CONSUME
            }
            val newCharges = currentCharges + 1
            level.setBlock(pos, refreshed.setValue(CHARGES, newCharges), 3)
            if (!player.isCreative) stack.shrink(1)
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f)
            player.displayClientMessage(
                Component.translatable(MSG_CHARGES, newCharges, 4),
                true
            )
        }
        return ItemInteractionResult.SUCCESS
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, movedByPiston: Boolean) {
        super.onPlace(state, level, pos, oldState, movedByPiston)
        if (!level.isClientSide) {
            VoidAnchorLogic.refreshActivation(level, pos, state)
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, movedByPiston: Boolean) {
        if (!state.`is`(newState.block)) {
            if (!level.isClientSide) {
                // 이 앵커를 쓰던 모든 플레이어의 좌표 초기화 + 알림
                level.players().forEach { player ->
                    if (player is ServerPlayer && player.voidAnchorPos == pos) {
                        player.voidAnchorPos = null
                        player.displayClientMessage(Component.translatable(MSG_DESTROYED), true)
                    }
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston)
        }
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(FACING)))
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = VoidAnchorBlockEntity(pos, state)

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(CHARGES, ACTIVE, FACING)
    }

    override fun codec(): MapCodec<VoidAnchorBlock> = CODEC
}
