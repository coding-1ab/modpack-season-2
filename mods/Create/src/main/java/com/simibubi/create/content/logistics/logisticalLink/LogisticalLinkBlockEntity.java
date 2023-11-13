package com.simibubi.create.content.logistics.logisticalLink;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.LogisticalWorkstationBlockEntity;
import com.simibubi.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LogisticalLinkBlockEntity extends LinkWithBulbBlockEntity {

	public static final AtomicInteger linkIdGenerator = new AtomicInteger();

	public int linkId;

	protected BlockPos targetOffset;

	private WeakReference<LogisticalWorkstationBlockEntity> target;

	public LogisticalLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		targetOffset = BlockPos.ZERO;
		linkId = linkIdGenerator.getAndIncrement();
		setLazyTickRate(10);
	}

	public InventorySummary fetchSummaryFromPackager() {
		BlockState blockState = getBlockState();
		if (blockState.getOptionalValue(LogisticalLinkBlock.POWERED)
			.orElse(true))
			return InventorySummary.EMPTY;
		BlockPos source = worldPosition.relative(blockState.getOptionalValue(LogisticalLinkBlock.FACING)
			.orElse(Direction.UP)
			.getOpposite());
		if (!(level.getBlockEntity(source) instanceof PackagerBlockEntity packager))
			return InventorySummary.EMPTY;
		sendPulseNextSync();
		sendData();
		return packager.getAvailableItems();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	public void onNoLongerPowered() {}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void lazyTick() {
		if (level.isClientSide())
			return;
		LogisticalWorkstationBlockEntity target = getTarget();
		if (target != null)
			target.keepConnected(this);
	}

	@Nullable
	public LogisticalWorkstationBlockEntity getTarget() {
		if (target != null) {
			LogisticalWorkstationBlockEntity workstationBlockEntity = target.get();
			if (workstationBlockEntity != null && !workstationBlockEntity.isRemoved()
				&& !workstationBlockEntity.isChunkUnloaded())
				return workstationBlockEntity;
			target = null;
		}
		BlockPos targetPos = worldPosition.offset(targetOffset);
		if (level.isLoaded(targetPos)
			&& level.getBlockEntity(targetPos) instanceof LogisticalWorkstationBlockEntity lwbe) {
			target = new WeakReference<LogisticalWorkstationBlockEntity>(lwbe);
			return lwbe;
		}
		return null;
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void writeSafe(CompoundTag tag) {
		super.writeSafe(tag);
		writeOffset(tag);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		writeOffset(tag);
	}

	private void writeOffset(CompoundTag tag) {
		tag.put("TargetOffset", NbtUtils.writeBlockPos(targetOffset));
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		targetOffset = NbtUtils.readBlockPos(tag.getCompound("TargetOffset"));
	}

}
