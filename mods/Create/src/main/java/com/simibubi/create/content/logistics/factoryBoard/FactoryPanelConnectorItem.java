package com.simibubi.create.content.logistics.factoryBoard;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelConnectorItem extends Item {

	static BlockPos connectingFrom;
	static Pointing connectingFromSide;

	public FactoryPanelConnectorItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		BlockState blockState = level.getBlockState(pos);

		if (!AllBlocks.FACTORY_PANEL.has(blockState))
			return super.useOn(pContext);
		if (!level.isClientSide())
			return InteractionResult.SUCCESS;

		Direction direction = FactoryPanelBlock.connectedDirection(blockState)
			.getOpposite();
		Vec3 clickLocation = pContext.getClickLocation();

		if (connectingFrom == null) {
			connectingFrom = pos;
			connectingFromSide = getTargetedSide(pos, direction, blockState, clickLocation);
			return InteractionResult.SUCCESS;
		}

		BlockPos connectingTo = pos;
		Pointing connectingToSide = getTargetedSide(pos, direction, blockState, clickLocation);

		if (connectingTo.equals(connectingFrom))
			return InteractionResult.SUCCESS;

		BlockPos connectingFromPos = connectingFrom;
		connectingFrom = null;

		BlockState blockStateFrom = level.getBlockState(connectingFromPos);

		if (!AllBlocks.FACTORY_PANEL.has(blockStateFrom))
			return InteractionResult.SUCCESS;
		if (blockStateFrom != blockState)
			return InteractionResult.SUCCESS;

		AllPackets.getChannel()
			.sendToServer(new FactoryPanelConnectionPacket(connectingFromPos, connectingFromSide, connectingTo,
				connectingToSide));

		return InteractionResult.SUCCESS;
	}

	private Pointing getTargetedSide(BlockPos pos, Direction direction, BlockState blockState, Vec3 clickLocation) {
		double bestDistance = 0;
		Direction bestDirection = null;
		Vec3 diffFromCenter = clickLocation.subtract(Vec3.atCenterOf(pos));
		for (Direction side : Iterate.directions) {
			if (side.getAxis() == direction.getAxis())
				continue;
			double diff = side.getAxis()
				.choose(diffFromCenter.x, diffFromCenter.y, diffFromCenter.z)
				* side.getAxisDirection()
					.getStep();
			if (diff < bestDistance)
				continue;
			bestDistance = diff;
			bestDirection = side;
		}

		Pointing result = Pointing.UP;
		for (Pointing p : Pointing.values())
			if (FactoryPanelBlock.getDirection(blockState, p) == bestDirection)
				result = p;

		return result;
	}

}
