package com.simibubi.create.content.logistics.packagerLink;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public class PackagerLinkBlockEntity extends LinkWithBulbBlockEntity {

	public LogisticallyLinkedBehaviour behaviour;
	public UUID placedBy;

	public PackagerLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
		placedBy = null;
	}

	public InventorySummary fetchSummaryFromPackager(@Nullable IItemHandler ignoredHandler) {
		PackagerBlockEntity packager = getPackager();
		if (packager == null || packager.defragmenterActive)
			return InventorySummary.EMPTY;
		if (packager.isTargetingSameInventory(ignoredHandler))
			return InventorySummary.EMPTY;

		sendPulseNextSync();
		sendData();
		return packager.getAvailableItems();
	}

	public Pair<PackagerBlockEntity, PackagingRequest> processRequest(ItemStack stack, int amount, String address,
		int linkIndex, MutableBoolean finalLink, int orderId, @Nullable PackageOrder orderContext,
		@Nullable IItemHandler ignoredHandler) {
		PackagerBlockEntity packager = getPackager();
		if (packager == null || packager.defragmenterActive)
			return null;
		if (packager.isTargetingSameInventory(ignoredHandler))
			return null;

		InventorySummary summary = packager.getAvailableItems();
		int availableCount = summary.getCountOf(stack);
		if (availableCount == 0)
			return null;

		sendPulseNextSync();
		sendData();

		int toWithdraw = Math.min(amount, availableCount);
		return Pair.of(packager,
			PackagingRequest.create(stack, toWithdraw, address, linkIndex, finalLink, 0, orderId, orderContext));
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if (placedBy != null)
			tag.putUUID("PlacedBy", placedBy);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		placedBy = tag.contains("PlacedBy") ? tag.getUUID("PlacedBy") : null;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, true));
	}

	@Override
	public void initialize() {
		super.initialize();
		behaviour.redstonePowerChanged(PackagerLinkBlock.getPower(getBlockState(), level, worldPosition));
		PackagerBlockEntity packager = getPackager();
		if (packager != null)
			packager.recheckIfLinksPresent();
	}

	@Nullable
	public PackagerBlockEntity getPackager() {
		BlockState blockState = getBlockState();
		if (behaviour.redstonePower == 15)
			return null;
		BlockPos source = worldPosition.relative(blockState.getOptionalValue(PackagerLinkBlock.FACING)
			.orElse(Direction.UP)
			.getOpposite());
		if (!(level.getBlockEntity(source) instanceof PackagerBlockEntity packager))
			return null;
		return packager;
	}

}
