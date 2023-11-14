package com.simibubi.create.content.logistics.packagerLink;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PackagerLinkBlock extends WrenchableDirectionalBlock implements IBE<PackagerLinkBlockEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public PackagerLinkBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockState placed = super.getStateForPlacement(context).setValue(FACING, face);
		BlockState placedOn = context.getLevel()
			.getBlockState(pos.relative(face.getOpposite()));
		if (!AllBlocks.PACKAGER.has(placedOn))
			return null;
		return placed.setValue(POWERED, shouldBePowered(placed, context.getLevel(), pos));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		boolean powered = shouldBePowered(state, worldIn, pos);
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != powered) {
			worldIn.setBlock(pos, state.cycle(POWERED), 2);
			withBlockEntityDo(worldIn, pos, link -> link.redstonePowerChanged(powered));
		}
	}

	private boolean shouldBePowered(BlockState state, Level worldIn, BlockPos pos) {
		boolean powered = false;
		for (Direction d : Iterate.directions) {
			if (d.getOpposite() == state.getValue(FACING))
				continue;
			if (worldIn.getSignal(pos.relative(d), d) == 0)
				continue;
			powered = true;
			break;
		}
		return powered;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.DATA_GATHERER.get(pState.getValue(FACING));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED));
	}

	@Override
	public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
		return false;
	}

	@Override
	public Class<PackagerLinkBlockEntity> getBlockEntityClass() {
		return PackagerLinkBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PackagerLinkBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PACKAGER_LINK.get();
	}

}
