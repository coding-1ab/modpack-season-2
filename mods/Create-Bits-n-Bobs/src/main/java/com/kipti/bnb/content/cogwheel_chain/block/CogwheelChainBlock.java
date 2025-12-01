package com.kipti.bnb.content.cogwheel_chain.block;

import com.kipti.bnb.registry.BnbBlockEntities;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CogwheelChainBlock extends RotatedPillarKineticBlock
        implements IBE<CogwheelChainBlockEntity>, SpecialBlockItemRequirement { //TODO : waterlog state

    protected CogwheelChainBlock(final boolean large, final Properties properties) {
        super(properties);
        isLarge = large;
    }

    public boolean isLargeChainCog() {
        return isLarge;
    }

    boolean isLarge;

    public static CogwheelChainBlock small(final Properties properties) {
        return new CogwheelChainBlock(false, properties);
    }

    public static CogwheelChainBlock large(final Properties properties) {
        return new CogwheelChainBlock(true, properties);
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final Player player = context.getPlayer();

        if (!(world instanceof final ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        final BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof final CogwheelChainBlockEntity cogwheelChainBE))
            return InteractionResult.SUCCESS;

        final ItemStack drops = cogwheelChainBE.destroyChain(player == null);
        if (player != null && !player.hasInfiniteMaterials())
            player.getInventory().placeItemBackInInventory(drops);
        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
        context.getLevel()
                .levelEvent(2001, context.getClickedPos(), Block.getId(state));
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context) {
        return (isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR).get(state.getValue(AXIS));
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader worldIn, final BlockPos pos) {
        return CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.getValue(AXIS));
    }

    @Override
    public void onRemove(final BlockState state, final Level world, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        IBE.onRemove(state, world, pos, newState);
    }

    @Override
    public Class<CogwheelChainBlockEntity> getBlockEntityClass() {
        return CogwheelChainBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CogwheelChainBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.COGWHEEL_CHAIN.get();
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    public float getRadius() {
        return isLarge ? 1f : 0.5f;
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, @Nullable final BlockEntity be) {
        return ItemRequirement.of(isLarge ? AllBlocks.LARGE_COGWHEEL.getDefaultState() : AllBlocks.COGWHEEL.getDefaultState(), be);
    }
}
