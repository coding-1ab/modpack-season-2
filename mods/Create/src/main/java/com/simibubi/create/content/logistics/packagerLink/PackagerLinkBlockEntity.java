package com.simibubi.create.content.logistics.packagerLink;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.stockTicker.LogisticalWorkstationBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PackagerLinkBlockEntity extends LinkWithBulbBlockEntity {

	public static final AtomicInteger linkIdGenerator = new AtomicInteger();

	public int linkId;

	protected BlockPos targetOffset;

	private WeakReference<LogisticalWorkstationBlockEntity> target;

	public PackagerLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		targetOffset = BlockPos.ZERO;
		linkId = linkIdGenerator.getAndIncrement();
		setLazyTickRate(10);
	}

	public InventorySummary fetchSummaryFromPackager() {
		PackagerBlockEntity packager = getSource();
		if (packager == null || packager.defragmenterActive)
			return InventorySummary.EMPTY;
		sendPulseNextSync();
		sendData();
		return packager.getAvailableItems();
	}

	public int processRequest(ItemStack stack, int amount, String address, int linkIndex, MutableBoolean finalLink,
		int orderId, @Nullable PackageOrder orderContext) {
		PackagerBlockEntity packager = getSource();
		if (packager == null || packager.defragmenterActive)
			return 0;

		InventorySummary summary = packager.getAvailableItems();
		int availableCount = summary.getCountOf(stack);
		if (availableCount == 0)
			return 0;

		int toWithdraw = Math.min(amount, availableCount);
		PackagingRequest packagingRequest =
			PackagingRequest.create(stack, toWithdraw, address, linkIndex, finalLink, 0, orderId, orderContext);
		packager.queueRequest(packagingRequest);
		sendPulseNextSync();
		sendData();

		return toWithdraw;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	public void redstonePowerChanged(boolean powered) {
		if (powered) {
			invalidateLink();
			return;
		}
		lazyTick();
	}

	@Override
	public void initialize() {
		super.initialize();
		PackagerBlockEntity source = getSource();
		if (source != null)
			source.recheckIfLinksPresent();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		invalidateLink();
	}

	private void invalidateLink() {
		LogisticalWorkstationBlockEntity target = getTarget();
		if (target == null)
			return;
		target.invalidateLink(this);
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

	@Nullable
	public PackagerBlockEntity getSource() {
		BlockState blockState = getBlockState();
		if (blockState.getOptionalValue(PackagerLinkBlock.POWERED)
			.orElse(true))
			return null;
		BlockPos source = worldPosition.relative(blockState.getOptionalValue(PackagerLinkBlock.FACING)
			.orElse(Direction.UP)
			.getOpposite());
		if (!(level.getBlockEntity(source) instanceof PackagerBlockEntity packager))
			return null;
		return packager;
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
