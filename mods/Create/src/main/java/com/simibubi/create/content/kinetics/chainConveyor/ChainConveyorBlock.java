package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChainConveyorBlock extends KineticBlock implements IBE<ChainConveyorBlockEntity> {

	public ChainConveyorBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return AllShapes.CHAIN_CONVEYOR_INTERACTION;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.CHAIN_CONVEYOR_INTERACTION;
	}

	@Override
	public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
		super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
		if (pLevel.isClientSide())
			return;
		if (!pPlayer.isCreative())
			return;
		withBlockEntityDo(pLevel, pPos, be -> be.cancelDrops = true);
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null)
			return super.onSneakWrenched(state, context);

		withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> {
			be.cancelDrops = true;
			if (player.isCreative())
				return;
			for (BlockPos targetPos : be.connections) {
				int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos);
				while (chainCost > 0) {
					player.getInventory()
						.placeItemBackInInventory(new ItemStack(Items.CHAIN, Math.min(chainCost, 64)));
					chainCost -= 64;
				}
			}
		});

		return super.onSneakWrenched(state, context);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		for (int x = -1; x <= 1; x++)
			for (int z = -1; z <= 1; z++)
				if (pContext.getLevel()
					.getBlockState(pContext.getClickedPos()
						.offset(x, 0, z))
					.getBlock() == this)
					return null;

		return super.getStateForPlacement(pContext);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos,
		CollisionContext pContext) {
		return Shapes.block();
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		ItemStack itemInHand = pPlayer.getItemInHand(pHand);
		if (!(itemInHand.getItem() instanceof PackageItem) && !itemInHand.isEmpty())
			return InteractionResult.CONSUME;
		if (pLevel.isClientSide())
			return InteractionResult.CONSUME;

		Vec3 diff = Vec3.atBottomCenterOf(pPos)
			.subtract(pPlayer.position());
		float angle = (float) (Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z) + 360 + 180) % 360;

		if (itemInHand.isEmpty()) {
			withBlockEntityDo(pLevel, pPos, be -> {
				float bestDiff = Float.POSITIVE_INFINITY;
				ChainConveyorPackage best = null;
				for (ChainConveyorPackage liftPackage : be.loopingPackages) {
					float angleDiff = Math.abs(AngleHelper.getShortestAngleDiff(liftPackage.chainPosition, angle));
					if (angleDiff > bestDiff)
						continue;
					bestDiff = angleDiff;
					best = liftPackage;
				}

				if (best == null)
					return;

				pPlayer.setItemInHand(pHand, best.item.copy());
				be.loopingPackages.remove(best);
				be.sendData();
			});

			return InteractionResult.CONSUME;
		}

		withBlockEntityDo(pLevel, pPos, be -> {
			if (!be.canAcceptMorePackages())
				return;
			be.addLoopingPackage(new ChainConveyorPackage(angle, itemInHand.copyWithCount(1)));
			itemInHand.shrink(1);
			if (itemInHand.isEmpty())
				pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
		});

		return InteractionResult.CONSUME;
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

	@Override
	public Class<ChainConveyorBlockEntity> getBlockEntityClass() {
		return ChainConveyorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ChainConveyorBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CHAIN_CONVEYOR.get();
	}

}
