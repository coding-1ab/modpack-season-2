package com.simibubi.create.content.logistics.packagerLink;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public class PackagerLinkBlockEntity extends LinkWithBulbBlockEntity {

	public LogisticallyLinkedBehaviour behaviour;

	public PackagerLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
	}

	public InventorySummary fetchSummaryFromPackager() {
		PackagerBlockEntity packager = getPackager();
		if (packager == null || packager.defragmenterActive)
			return InventorySummary.EMPTY;
		sendPulseNextSync();
		sendData();
		return packager.getAvailableItems();
	}

	public int processRequest(ItemStack stack, int amount, String address, int linkIndex, MutableBoolean finalLink,
		int orderId, @Nullable PackageOrder orderContext, @Nullable IItemHandler ignoredHandler) {
		PackagerBlockEntity packager = getPackager();
		if (packager == null || packager.defragmenterActive)
			return 0;
		if (packager.isTargetingSameInventory(ignoredHandler))
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
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this));
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
