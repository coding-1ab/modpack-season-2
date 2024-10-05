package com.simibubi.create.content.logistics.packagePort.frogport;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.foundation.item.ItemHelper;

import net.createmod.catnip.utility.Iterate;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class FrogportBlockEntity extends PackagePortBlockEntity {

	public ItemStack animatedPackage;
	public LerpedFloat animationProgress;
	public LerpedFloat anticipationProgress;
	public boolean currentlyDepositing;

	public boolean sendAnticipate;

	public float passiveYaw;

	public FrogportBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		animationProgress = LerpedFloat.linear();
		anticipationProgress = LerpedFloat.linear();
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
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide() || isAnimationInProgress())
			return;
		tryPushingToAdjacentInventories();
		tryPullingFromOwnAndAdjacentInventories();
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
					if (target == null
						|| !target.depositImmediately() && !target.export(level, worldPosition, animatedPackage, false))
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
			if (!ItemHandlerHelper.insertItem(inventory, animatedPackage.copy(), false)
				.isEmpty())
				drop(animatedPackage);
		}

		animatedPackage = null;
	}

	public void startAnimation(ItemStack box, boolean deposit) {
		if (!(box.getItem() instanceof PackageItem))
			return;

		if (deposit && (target == null
			|| target.depositImmediately() && !target.export(level, worldPosition, box.copy(), false)))
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

	public void tryPullingFromOwnAndAdjacentInventories() {
		if (isAnimationInProgress())
			return;
		if (target == null || !target.export(level, worldPosition, AllItems.CARDBOARD_PACKAGE_10x12.asStack(), true))
			return;
		if (tryPullingFrom(inventory))
			return;
		for (Direction side : Iterate.directions) {
			if (side != Direction.DOWN)
				continue;
			IItemHandler handler = getAdjacentInventory(side);
			if (handler == null)
				continue;
			if (tryPullingFrom(handler))
				return;
		}
	}

	public boolean tryPullingFrom(IItemHandler handler) {
		ItemStack extract = ItemHelper.extract(handler, stack -> {
			if (!PackageItem.isPackage(stack))
				return false;
			String filterString = getFilterString();
			return filterString == null || handler instanceof PackagerItemHandler
				|| !PackageItem.matchAddress(stack, filterString);
		}, false);
		if (extract.isEmpty())
			return false;
		startAnimation(extract, true);
		return true;

	}

	protected IItemHandler getAdjacentInventory(Direction side) {
		BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
		if (blockEntity == null || blockEntity instanceof FrogportBlockEntity)
			return null;
		return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite())
			.orElse(null);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
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
		passiveYaw = tag.getFloat("PlacedYaw");
		if (!clientPacket)
			animatedPackage = null;
		if (tag.contains("AnimatedPackage"))
			startAnimation(ItemStack.of(tag.getCompound("AnimatedPackage")), tag.getBoolean("Deposit"));
		if (clientPacket && tag.contains("Anticipate"))
			anticipate();
	}

	public float getYaw() {
		if (target == null)
			return passiveYaw;
		Vec3 diff = target.getExactTargetLocation(this, level, worldPosition)
			.subtract(Vec3.atCenterOf(worldPosition));
		return (float) (Mth.atan2(diff.x, diff.z) * Mth.RAD_TO_DEG) + 180;
	}

}
