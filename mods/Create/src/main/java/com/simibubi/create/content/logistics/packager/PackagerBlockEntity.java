package com.simibubi.create.content.logistics.packager;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlock.PackagerType;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class PackagerBlockEntity extends SmartBlockEntity {

	public boolean redstoneModeActive;
	public boolean redstonePowered;
	public String signBasedAddress;

	public InvManipulationBehaviour targetInventory;
	public ItemStack heldBox;
	public ItemStack previouslyUnwrapped;

	public List<PackagingRequest> queuedRequests;

	public PackagerItemHandler inventory;
	private final LazyOptional<IItemHandler> invProvider;

	public static final int CYCLE = 30;
	public int animationTicks;
	public boolean animationInward;

	private InventorySummary availableItems;
	private VersionedInventoryTrackerBehaviour invVersionTracker;

	//

	public boolean defragmenterActive;
	public PackageDefragmenter defragmenter;

	public PackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		redstonePowered = state.getOptionalValue(PackagerBlock.POWERED)
			.orElse(false);
		redstoneModeActive = true;
		heldBox = ItemStack.EMPTY;
		previouslyUnwrapped = ItemStack.EMPTY;
		inventory = new PackagerItemHandler(this);
		invProvider = LazyOptional.of(() -> inventory);
		animationTicks = 0;
		animationInward = true;
		queuedRequests = new LinkedList<>();
		signBasedAddress = "";
		if (AllBlocks.PACKAGER.has(state))
			defragmenterActive = state.getValue(PackagerBlock.TYPE) == PackagerType.DEFRAG;
		defragmenter = new PackageDefragmenter();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(targetInventory = new InvManipulationBehaviour(this, InterfaceProvider.oppositeOfBlockFacing()));
		behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
	}

	@Override
	public void initialize() {
		super.initialize();
		recheckIfLinksPresent();
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide() && !queuedRequests.isEmpty() && !defragmenterActive)
			attemptToSend(true);
		if (animationTicks == 0) {
			previouslyUnwrapped = ItemStack.EMPTY;
			return;
		}

		animationTicks--;

		if (animationTicks == 0 && !level.isClientSide())
			wakeTheFrogs();
	}

	public void queueRequest(PackagingRequest packagingRequest) {
		queuedRequests.add(packagingRequest);
		level.blockEntityChanged(worldPosition);
	}

	public InventorySummary getAvailableItems() {
		if (availableItems != null && invVersionTracker.stillWaiting(targetInventory.getInventory()))
			return availableItems;
		availableItems = new InventorySummary();

		IItemHandler targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return availableItems;

		for (int slot = 0; slot < targetInv.getSlots(); slot++)
			availableItems.add(targetInv.getStackInSlot(slot));

		invVersionTracker.awaitNewVersion(targetInventory.getInventory());
		return availableItems;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (!redstonePowered || level.isClientSide())
			return;
		redstonePowered = getBlockState().getOptionalValue(PackagerBlock.POWERED)
			.orElse(false);
		if (!redstonePowered)
			return;
		recheckIfLinksPresent();
		if (!redstoneModeActive)
			return;
		updateSignAddress();
		attemptToSend(false);
	}

	public void recheckIfLinksPresent() {
		redstoneModeActive = true;
		if (defragmenterActive)
			return;
		for (Direction d : Iterate.directions) {
			BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
			if (!AllBlocks.PACKAGER_LINK.has(adjacentState))
				continue;
			if (adjacentState.getValue(PackagerLinkBlock.FACING) != d)
				continue;
			redstoneModeActive = false;
			return;
		}
	}

	public void activate() {
		redstonePowered = true;
		setChanged();
	}

	public boolean unwrapBox(ItemStack box, boolean simulate) {
		if (animationTicks > 0)
			return false;

		ItemStackHandler contents = PackageItem.getContents(box);
		IItemHandler targetInv = targetInventory.getInventory();
		if (targetInv == null)
			return false;

		boolean targetIsCreativeCrate = targetInv instanceof BottomlessItemHandler;

		if (defragmenterActive) {
			boolean anySpace = false;
			for (int slot = 0; slot < targetInv.getSlots(); slot++) {
				ItemStack remainder = targetInv.insertItem(slot, box, simulate);
				if (!remainder.isEmpty())
					continue;
				anySpace = true;
				break;
			}

			if (!targetIsCreativeCrate && !anySpace)
				return false;
		}

		if (!defragmenterActive) {
			for (int slot = 0; slot < targetInv.getSlots(); slot++) {
				ItemStack itemInSlot = targetInv.getStackInSlot(slot);
				int itemsAddedToSlot = 0;
				for (int boxSlot = 0; boxSlot < contents.getSlots(); boxSlot++) {
					ItemStack toInsert = contents.getStackInSlot(boxSlot);
					if (toInsert.isEmpty())
						continue;
					if (targetInv.insertItem(slot, toInsert, true)
						.getCount() == toInsert.getCount())
						continue;
					if (itemInSlot.isEmpty()) {
						int maxStackSize = targetInv.getSlotLimit(slot);
						if (maxStackSize < toInsert.getCount()) {
							toInsert.shrink(maxStackSize);
							toInsert = ItemHandlerHelper.copyStackWithSize(toInsert, maxStackSize);
						} else
							contents.setStackInSlot(boxSlot, ItemStack.EMPTY);
						itemInSlot = toInsert;
						targetInv.insertItem(slot, toInsert, simulate);
						continue;
					}
					if (!ItemHandlerHelper.canItemStacksStack(toInsert, itemInSlot))
						continue;
					int insertedAmount = toInsert.getCount() - targetInv.insertItem(slot, toInsert, simulate)
						.getCount();
					int slotLimit = (int) ((targetInv.getStackInSlot(slot)
						.isEmpty() ? itemInSlot.getMaxStackSize() / 64f : 1) * targetInv.getSlotLimit(slot));
					int insertableAmountWithPreviousItems =
						Math.min(toInsert.getCount(), slotLimit - itemInSlot.getCount() - itemsAddedToSlot);
					int added = Math.min(insertedAmount, insertableAmountWithPreviousItems);
					contents.setStackInSlot(boxSlot,
						ItemHandlerHelper.copyStackWithSize(toInsert, toInsert.getCount() - added));
				}
			}

			if (!targetIsCreativeCrate)
				for (int boxSlot = 0; boxSlot < contents.getSlots(); boxSlot++)
					if (!contents.getStackInSlot(boxSlot)
						.isEmpty())
						return false;
		}

		if (simulate)
			return true;

		previouslyUnwrapped = box;
		animationInward = true;
		animationTicks = CYCLE;
		notifyUpdate();
		return true;
	}

	public void attemptToSend(boolean requestQueue) {
		if (!heldBox.isEmpty() || animationTicks != 0)
			return;

		IItemHandler targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return;

		if (defragmenterActive) {
			attemptToDefrag(targetInv);
			return;
		}

		boolean anyItemPresent = false;
		ItemStackHandler extractedItems = new ItemStackHandler(PackageItem.SLOTS);
		ItemStack extractedPackageItem = ItemStack.EMPTY;
		PackagingRequest nextRequest = null;
		String fixedAddress = null;
		int fixedOrderId = 0;

		boolean continuePacking = true;

		// Data written to packages for defrags
		int linkIndexInOrder = 0;
		boolean finalLinkInOrder = false;
		int packageIndexAtLink = 0;
		boolean finalPackageAtLink = false;
		PackageOrder orderContext = null;

		if (requestQueue && !queuedRequests.isEmpty()) {
			nextRequest = queuedRequests.get(0);
			fixedAddress = nextRequest.address();
			fixedOrderId = nextRequest.orderId();
			linkIndexInOrder = nextRequest.linkIndex();
			finalLinkInOrder = nextRequest.finalLink()
				.booleanValue();
			packageIndexAtLink = nextRequest.packageCounter()
				.getAndIncrement();
			orderContext = nextRequest.context();
		}

		Outer: for (int i = 0; i < PackageItem.SLOTS; i++) {
			while (continuePacking) {
				continuePacking = false;

				for (int slot = 0; slot < targetInv.getSlots(); slot++) {
					int initialCount = requestQueue ? Math.min(64, nextRequest.getCount()) : 64;
					ItemStack extracted = targetInv.extractItem(slot, initialCount, true);
					if (extracted.isEmpty())
						continue;
					if (requestQueue && !ItemHandlerHelper.canItemStacksStack(extracted, nextRequest.item()))
						continue;

					boolean bulky = !extracted.getItem()
						.canFitInsideContainerItems();
					if (bulky && anyItemPresent)
						continue;

					anyItemPresent = true;
					int leftovers = ItemHandlerHelper.insertItemStacked(extractedItems, extracted.copy(), false)
						.getCount();
					int transferred = extracted.getCount() - leftovers;
					targetInv.extractItem(slot, transferred, false);

					if (extracted.getItem() instanceof PackageItem)
						extractedPackageItem = extracted;

					if (!requestQueue) {
						if (targetInv instanceof BottomlessItemHandler) {
							continuePacking = true;
							continue Outer;
						}
						if (bulky)
							break Outer;
						continue;
					}

					nextRequest.subtract(transferred);

					if (!nextRequest.isEmpty()) {
						if (bulky)
							break Outer;
						continue;
					}

					finalPackageAtLink = true;
					queuedRequests.remove(0);
					if (queuedRequests.isEmpty())
						break Outer;
					int previousCount = nextRequest.packageCounter()
						.intValue();
					nextRequest = queuedRequests.get(0);
					if (!fixedAddress.equals(nextRequest.address()))
						break Outer;
					if (fixedOrderId != nextRequest.orderId())
						break Outer;

					nextRequest.packageCounter()
						.setValue(previousCount);
					finalPackageAtLink = false;
					continuePacking = true;
					if (bulky)
						break Outer;
					break;
				}
			}
		}

		if (!anyItemPresent) {
			if (nextRequest != null)
				queuedRequests.remove(0);
			return;
		}

		heldBox = extractedPackageItem.isEmpty() ? PackageItem.containing(extractedItems) : extractedPackageItem.copy();
		PackageItem.clearAddress(heldBox);

		if (fixedAddress != null)
			PackageItem.addAddress(heldBox, fixedAddress);
		if (requestQueue)
			PackageItem.setOrder(heldBox, fixedOrderId, linkIndexInOrder, finalLinkInOrder, packageIndexAtLink,
				finalPackageAtLink, orderContext);
		if (!requestQueue && !signBasedAddress.isBlank())
			PackageItem.addAddress(heldBox, signBasedAddress);

		animationInward = false;
		animationTicks = CYCLE;

		notifyUpdate();
	}

	protected void attemptToDefrag(IItemHandler targetInv) {
		defragmenter.clear();
		int completedOrderId = -1;

		for (int slot = 0; slot < targetInv.getSlots(); slot++) {
			ItemStack extracted = targetInv.extractItem(slot, 1, true);
			if (extracted.isEmpty() || !PackageItem.isPackage(extracted))
				continue;

			if (!defragmenter.isFragmented(extracted)) {
				targetInv.extractItem(slot, 1, false);
				heldBox = extracted.copy();
				animationInward = false;
				animationTicks = CYCLE;
				notifyUpdate();
				return;
			}

			completedOrderId = defragmenter.addPackageFragment(extracted);
			if (completedOrderId != -1)
				break;
		}

		if (completedOrderId == -1)
			return;

		List<ItemStack> boxesToExport = defragmenter.repack(completedOrderId);

		for (int slot = 0; slot < targetInv.getSlots(); slot++) {
			ItemStack extracted = targetInv.extractItem(slot, 1, true);
			if (extracted.isEmpty() || !PackageItem.isPackage(extracted))
				continue;
			if (PackageItem.getOrderId(extracted) != completedOrderId)
				continue;
			targetInv.extractItem(slot, 1, false);
		}

		if (boxesToExport.isEmpty())
			return;

		heldBox = boxesToExport.get(0)
			.copy();
		animationInward = false;
		animationTicks = CYCLE;

		for (int i = 1; i < boxesToExport.size(); i++)
			ItemHandlerHelper.insertItem(targetInv, boxesToExport.get(i), false);

		notifyUpdate();
	}

	protected void updateSignAddress() {
		signBasedAddress = "";
		for (Direction side : Iterate.directions) {
			String address = getSign(side);
			if (address == null || address.isBlank())
				continue;
			signBasedAddress = address;
		}
	}

	protected String getSign(Direction side) {
		BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
		if (!(blockEntity instanceof SignBlockEntity sign))
			return null;
		for (boolean front : Iterate.trueAndFalse) {
			SignText text = sign.getText(front);
			for (Component component : text.getMessages(false)) {
				String address = component.getString();
				if (!address.isBlank())
					return address;
			}
		}
		return null;
	}

	protected void wakeTheFrogs() {
		if (level.getBlockEntity(worldPosition.relative(Direction.UP)) instanceof PackagePortBlockEntity port)
			port.tryPullingFromAdjacentInventories();
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		redstonePowered = compound.getBoolean("Active");
		animationInward = compound.getBoolean("AnimationInward");
		animationTicks = compound.getInt("AnimationTicks");
		signBasedAddress = compound.getString("SignAddress");
		heldBox = ItemStack.of(compound.getCompound("HeldBox"));
		previouslyUnwrapped = ItemStack.of(compound.getCompound("InsertedBox"));
		defragmenterActive = compound.getBoolean("Defrag");
		if (clientPacket)
			return;
		queuedRequests =
			NBTHelper.readCompoundList(compound.getList("QueuedRequests", Tag.TAG_COMPOUND), PackagingRequest::fromNBT);
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putBoolean("Active", redstonePowered);
		compound.putBoolean("AnimationInward", animationInward);
		compound.putInt("AnimationTicks", animationTicks);
		compound.putString("SignAddress", signBasedAddress);
		compound.put("HeldBox", heldBox.serializeNBT());
		compound.put("InsertedBox", previouslyUnwrapped.serializeNBT());
		compound.putBoolean("Defrag", defragmenterActive);
		if (clientPacket)
			return;
		compound.put("QueuedRequests", NBTHelper.writeCompoundList(queuedRequests, PackagingRequest::toNBT));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		invProvider.invalidate();
	}

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(level, worldPosition, inventory);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER)
			return invProvider.cast();
		return super.getCapability(cap, side);
	}

	public float getTrayOffset(float partialTicks) {
		float progress = Mth.clamp(Math.max(0, animationTicks - 5 - partialTicks) / (CYCLE * .75f) * 2 - 1, -1, 1);
		progress = 1 - progress * progress;
		return progress * progress;
	}

	public ItemStack getRenderedBox() {
		if (animationInward)
			return animationTicks <= CYCLE / 2 ? ItemStack.EMPTY : previouslyUnwrapped;
		return animationTicks >= CYCLE / 2 ? ItemStack.EMPTY : heldBox;
	}

}
