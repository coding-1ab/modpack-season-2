package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.createmod.catnip.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FactoryPanelBlock extends FaceAttachedHorizontalDirectionalBlock
	implements ProperWaterloggedBlock, IBE<FactoryPanelBlockEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public FactoryPanelBlock(Properties p_53182_) {
		super(p_53182_);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACE, FACING, WATERLOGGED, POWERED));
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return canAttachLenient(pLevel, pPos, getConnectedDirection(pState).getOpposite());
	}

	public static boolean canAttachLenient(LevelReader pReader, BlockPos pPos, Direction pDirection) {
		BlockPos blockpos = pPos.relative(pDirection);
		return !pReader.getBlockState(blockpos)
			.getCollisionShape(pReader, blockpos)
			.isEmpty();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState stateForPlacement = super.getStateForPlacement(pContext);
		if (stateForPlacement == null)
			return null;
		if (stateForPlacement.getValue(FACE) == AttachFace.FLOOR)
			stateForPlacement = stateForPlacement.setValue(FACING, stateForPlacement.getValue(FACING)
				.getOpposite());
		return withWater(stateForPlacement, pContext);
	}

	@Override
	public boolean isSignalSource(BlockState pState) {
		return true;
	}

	@Override
	public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return pBlockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return pBlockState.getValue(POWERED) && getConnectedDirection(pBlockState) == pSide ? 15 : 0;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.FACTORY_PANEL.get(getConnectedDirection(pState));
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
		BlockPos pCurrentPos, BlockPos pFacingPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	public static Direction connectedDirection(BlockState state) {
		return getConnectedDirection(state);
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		if (pPlayer != null && AllItems.FACTORY_PANEL_CONNECTOR.isIn(pPlayer.getItemInHand(pHand)))
			return InteractionResult.PASS;
		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		boolean blockChanged = !pState.is(pNewState.getBlock());
		if (!pIsMoving && blockChanged)
			if (pState.getValue(POWERED))
				updateNeighbours(pState, pLevel, pPos);

		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	public static void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
		pLevel.updateNeighborsAt(pPos, pState.getBlock());
		pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), pState.getBlock());
	}

	public static Direction getDirection(BlockState state, Pointing side) {
		Direction direction = getConnectedDirection(state).getOpposite();
		Direction hDirection = state.getValue(FACING);

		if (direction == Direction.UP)
			side = Pointing.values()[(side.ordinal() + hDirection.getOpposite()
				.get2DDataValue()) % 4];
		if (direction == Direction.DOWN)
			side = Pointing.values()[(side.ordinal() + 6 - hDirection.get2DDataValue()) % 4];

		return side.getCombinedDirection(direction);
	}

	@Override
	public Class<FactoryPanelBlockEntity> getBlockEntityClass() {
		return FactoryPanelBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends FactoryPanelBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.FACTORY_PANEL.get();
	}

}
