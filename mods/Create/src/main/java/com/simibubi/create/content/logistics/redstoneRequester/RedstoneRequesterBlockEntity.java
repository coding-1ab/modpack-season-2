package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.List;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneRequesterBlockEntity extends SmartBlockEntity {

	protected PackageOrder encodedRequest;
	protected String encodedTargetAdress;
	protected boolean redstonePowered;
	protected BlockPos targetOffset;
	protected String targetDim;
	protected boolean isValid;

	public RedstoneRequesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		encodedRequest = PackageOrder.empty();
		encodedTargetAdress = "";
		targetOffset = BlockPos.ZERO;
		targetDim = "";
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	protected void onRedstonePowerChanged() {
		boolean hasNeighborSignal = level.hasNeighborSignal(worldPosition);
		if (redstonePowered == hasNeighborSignal)
			return;

		if (hasNeighborSignal)
			triggerRequest();

		redstonePowered = hasNeighborSignal;
		setChanged();
	}

	private void triggerRequest() {
		BlockPos tickerPos = worldPosition.offset(targetOffset);
		if (!level.isLoaded(tickerPos))
			return;
		BlockEntity blockEntity = level.getBlockEntity(tickerPos);
		if (blockEntity instanceof StockTickerBlockEntity stbe)
			stbe.broadcastPackageRequest(encodedRequest, null, encodedTargetAdress);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		redstonePowered = tag.getBoolean("Powered");
		targetOffset = NbtUtils.readBlockPos(tag.getCompound("TargetOffset"));
		targetDim = tag.getString("TargetDim");
		isValid = tag.getBoolean("Valid");
		
		encodedTargetAdress = tag.getString("EncodedAddress");
		encodedRequest = PackageOrder.read(tag.getCompound("EncodedRequest"));
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putBoolean("Powered", redstonePowered);
		tag.put("TargetOffset", NbtUtils.writeBlockPos(targetOffset));
		tag.putString("TargetDim", targetDim);
		tag.putBoolean("Valid", isValid);
		
		tag.putString("EncodedAddress", encodedTargetAdress);
		tag.put("EncodedRequest", encodedRequest.write());

	}

}
