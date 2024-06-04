package com.simibubi.create.content.logistics.packagePort;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class PackagePortBlockEntity extends SmartBlockEntity {

	public FilteringBehaviour filtering;
	public PackagePortTarget target;
	public PackagePortInventory inventory;
	private LazyOptional<IItemHandler> itemHandler;

	public PackagePortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inventory = new PackagePortInventory(this);
		itemHandler = LazyOptional.of(() -> inventory);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = new FilteringBehaviour(this, new PackagePortFilterSlotPositioning())
			.withPredicate(AllItems.PACKAGE_FILTER::isIn)
			.withCallback(this::filterChanged));
	}

	@Override
	public AABB getRenderBoundingBox() {
		return super.getRenderBoundingBox().expandTowards(0, 1, 0);
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (isItemHandlerCap(cap))
			return itemHandler.cast();
		return super.getCapability(cap, side);
	}

	private void filterChanged(ItemStack filter) {
		if (target != null) {
			target.deregister(this, level, worldPosition);
			target.register(this, level, worldPosition);
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (target != null)
			target.register(this, level, worldPosition);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (target != null)
			target.deregister(this, level, worldPosition);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if (target != null)
			tag.put("Target", target.write());
		tag.put("Inventory", inventory.serializeNBT());
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		target = PackagePortTarget.read(tag.getCompound("Target"));
		inventory.deserializeNBT(tag.getCompound("Inventory"));
	}

}
