package com.simibubi.create.content.logistics.packagePort;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.filter.FilterItemStack.PackageFilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class PackagePortBlockEntity extends SmartBlockEntity {

	public FilteringBehaviour filtering;
	public PackagePortTarget target;
	public PackagePortInventory inventory;
	private LazyOptional<IItemHandler> itemHandler;

	public ItemStack animatedPackage;
	public LerpedFloat animationProgress;
	public LerpedFloat anticipationProgress;
	public boolean currentlyDepositing;

	public boolean sendAnticipate;

	public float passiveYaw;

	public PackagePortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inventory = new PackagePortInventory(this);
		itemHandler = LazyOptional.of(() -> inventory);
		animationProgress = LerpedFloat.linear();
		anticipationProgress = LerpedFloat.linear();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = new FilteringBehaviour(this, new PackagePortFilterSlotPositioning())
			.withPredicate(AllItems.PACKAGE_FILTER::isIn)
			.withCallback(this::filterChanged));
	}

	public boolean isAnimationInProgress() {
		return animationProgress.getChaseTarget() == 1;
	}

	@Override
	public AABB getRenderBoundingBox() {
		AABB bb = super.getRenderBoundingBox().expandTowards(0, 1, 0);
		if (target != null)
			bb = bb.minmax(new AABB(BlockPos.containing(target.getExactTargetLocation(this, level, worldPosition))))
				.inflate(0.5);
		return bb;
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
		if (level.isClientSide() || isAnimationInProgress())
			return;
		tryPushingToAdjacentInventories();
		tryPullingFromAdjacentInventories();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (target != null)
			target.deregister(this, level, worldPosition);
		for (int i = 0; i < inventory.getSlots(); i++)
			drop(inventory.getStackInSlot(i));
	}

	public void sendAnticipate() {
		if (isAnimationInProgress())
			return;
		sendAnticipate = true;
		sendData();
	}

	public void anticipate() {
		anticipationProgress.chase(1, 0.1, Chaser.LINEAR);
	}

	@Override
	public void tick() {
		super.tick();

		if (anticipationProgress.getValue() == 1)
			anticipationProgress.updateChaseTarget(0);

		anticipationProgress.tickChaser();

		if (!isAnimationInProgress())
			return;

		animationProgress.tickChaser();

		if (currentlyDepositing) {
			if (!level.isClientSide()) {
				if (animationProgress.getValue() > 0.5 && animatedPackage != null) {
					if (target == null || !target.export(level, worldPosition, animatedPackage, false))
						drop(animatedPackage);
					animatedPackage = null;
				}
			} else {
				if (animationProgress.getValue() > 0.7)
					animatedPackage = null;
			}
		}

		if (animationProgress.getValue() < 1)
			return;

		anticipationProgress.startWithValue(0);
		animationProgress.startWithValue(0);
		if (level.isClientSide()) {
			animatedPackage = null;
			return;
		}

		if (!currentlyDepositing) {
			inventory.receiveMode(true);
			if (!ItemHandlerHelper.insertItem(inventory, animatedPackage.copy(), false)
				.isEmpty())
				drop(animatedPackage);
			inventory.receiveMode(false);
		}

		animatedPackage = null;
	}

	public void drop(ItemStack box) {
		if (box.isEmpty())
			return;
		level.addFreshEntity(PackageEntity.fromItemStack(level, VecHelper.getCenterOf(worldPosition), box));
	}

	public void startAnimation(ItemStack box, boolean deposit) {
		if (!(box.getItem() instanceof PackageItem))
			return;

		animationProgress.startWithValue(0);
		animationProgress.chase(1, 0.1, Chaser.LINEAR);
		animatedPackage = box;
		currentlyDepositing = deposit;

		if (level != null && level.isClientSide() && !currentlyDepositing) {
			Vec3 vec = target.getExactTargetLocation(this, level, worldPosition);
			if (vec != null) {
				for (int i = 0; i < 5; i++) {
					level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, AllBlocks.ROPE.getDefaultState()),
						vec.x, vec.y - level.random.nextFloat() * 0.25, vec.z, 0, 0, 0);
				}
			}
		}

		if (level != null && !level.isClientSide()) {
			level.blockEntityChanged(worldPosition);
			sendData();
		}
	}

	protected void tryPushingToAdjacentInventories() {
		if (inventory.isEmpty())
			return;
		for (Direction side : Iterate.directions) {
			if (side != Direction.DOWN)
				continue;
			IItemHandler handler = getAdjacentInventory(side);
			if (handler == null)
				continue;
			boolean anyLeft = false;
			for (int i = 0; i < inventory.getSlots(); i++) {
				ItemStack stackInSlot = inventory.getStackInSlot(i);
				if (stackInSlot.isEmpty())
					continue;
				ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stackInSlot, false);
				if (remainder.isEmpty()) {
					inventory.setStackInSlot(i, ItemStack.EMPTY);
					level.blockEntityChanged(worldPosition);
				} else
					anyLeft = true;
			}
			if (!anyLeft)
				break;
		}
	}

	public void tryPullingFromAdjacentInventories() {
		if (isAnimationInProgress())
			return;
		if (target == null || !target.export(level, worldPosition, AllItems.CARDBOARD_PACKAGE_10x12.asStack(), true))
			return;
		for (Direction side : Iterate.directions) {
			if (side != Direction.DOWN)
				continue;
			IItemHandler handler = getAdjacentInventory(side);
			if (handler == null)
				continue;
			ItemStack extract = ItemHelper.extract(handler, stack -> {
				if (!PackageItem.isPackage(stack))
					return false;
				String filterString = getFilterString();
				return filterString == null || !PackageItem.matchAddress(stack, filterString);
			}, false);
			if (extract.isEmpty())
				continue;
			startAnimation(extract, true);
			return;
		}
	}

	protected IItemHandler getAdjacentInventory(Direction side) {
		BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
		if (blockEntity == null || blockEntity instanceof PackagePortBlockEntity)
			return null;
		return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite())
			.orElse(null);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if (target != null)
			tag.put("Target", target.write());
		tag.put("Inventory", inventory.serializeNBT());
		tag.putFloat("PlacedYaw", passiveYaw);
		if (animatedPackage != null) {
			tag.put("AnimatedPackage", animatedPackage.serializeNBT());
			tag.putBoolean("Deposit", currentlyDepositing);
		}
		if (sendAnticipate) {
			sendAnticipate = false;
			tag.putBoolean("Anticipate", true);
		}
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		PackagePortTarget prevTarget = target;
		target = PackagePortTarget.read(tag.getCompound("Target"));
		inventory.deserializeNBT(tag.getCompound("Inventory"));
		passiveYaw = tag.getFloat("PlacedYaw");
		if (!clientPacket)
			animatedPackage = null;
		if (tag.contains("AnimatedPackage"))
			startAnimation(ItemStack.of(tag.getCompound("AnimatedPackage")), tag.getBoolean("Deposit"));
		if (clientPacket && tag.contains("Anticipate"))
			anticipate();
		if (clientPacket && prevTarget != target)
			invalidateRenderBoundingBox();
	}

	public String getFilterString() {
		ItemStack filter = filtering.getFilter();
		String portFilter = null;
		FilterItemStack filterStack = FilterItemStack.of(filter);
		if (AllItems.PACKAGE_FILTER.isIn(filter)) {
			if (!(filterStack instanceof PackageFilterItemStack pfis))
				return "";
			portFilter = pfis.filterString;
		}
		return portFilter;
	}

	public float getYaw() {
		if (target == null)
			return passiveYaw;
		Vec3 diff = target.getExactTargetLocation(this, level, worldPosition)
			.subtract(Vec3.atCenterOf(worldPosition));
		return (float) (Mth.atan2(diff.x, diff.z) * Mth.RAD_TO_DEG) + 180;
	}

}
