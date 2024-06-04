package com.simibubi.create.content.kinetics.chainLift;

import com.simibubi.create.AllBlockEntityTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ChainLiftBlock extends KineticBlock implements IBE<ChainLiftBlockEntity> {

	public ChainLiftBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		ItemStack itemInHand = pPlayer.getItemInHand(pHand);
		if (!(itemInHand.getItem() instanceof PackageItem) && !itemInHand.isEmpty())
			return InteractionResult.PASS;
		if (pLevel.isClientSide())
			return InteractionResult.CONSUME;

		Vec3 diff = Vec3.atBottomCenterOf(pPos)
			.subtract(pPlayer.position());
		float angle = (float) (Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z) + 360 + 180) % 360;

		if (itemInHand.isEmpty()) {
			withBlockEntityDo(pLevel, pPos, be -> {
				float bestDiff = Float.POSITIVE_INFINITY;
				ChainLiftPackage best = null;
				for (ChainLiftPackage liftPackage : be.loopingPackages) {
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
			be.addLoopingPackage(new ChainLiftPackage(angle, itemInHand.copyWithCount(1)));
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
	public Class<ChainLiftBlockEntity> getBlockEntityClass() {
		return ChainLiftBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ChainLiftBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CHAIN_LIFT.get();
	}

}
