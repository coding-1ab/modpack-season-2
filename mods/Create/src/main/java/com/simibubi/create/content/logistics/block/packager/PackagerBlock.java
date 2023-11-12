package com.simibubi.create.content.logistics.block.packager;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PackagerBlock extends HorizontalDirectionalBlock implements IBE<PackagerBlockEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public PackagerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(POWERED, context.getLevel()
			.hasNeighborSignal(context.getClickedPos()))
			.setValue(FACING, context.getHorizontalDirection()
				.getOpposite());
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (player != null && AllItems.WRENCH.isIn(player.getItemInHand(handIn)))
			return InteractionResult.PASS;

		if (onBlockEntityUse(worldIn, pos, be -> {
			if (be.heldBox.isEmpty())
				return InteractionResult.PASS;
			if (be.animationTicks > 0)
				return InteractionResult.PASS;
			if (!worldIn.isClientSide()) {
				player.getInventory()
					.placeItemBackInInventory(be.heldBox.copy());
				be.heldBox = ItemStack.EMPTY;
				be.notifyUpdate();
			}
			return InteractionResult.SUCCESS;
		}).consumesAction())
			return InteractionResult.SUCCESS;

		return InteractionResult.PASS;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED, FACING));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered == worldIn.hasNeighborSignal(pos))
			return;
		worldIn.setBlock(pos, state.cycle(POWERED), 2);
		if (!previouslyPowered)
			withBlockEntityDo(worldIn, pos, PackagerBlockEntity::activate);
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public Class<PackagerBlockEntity> getBlockEntityClass() {
		return PackagerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PACKAGER.get();
	}

}
