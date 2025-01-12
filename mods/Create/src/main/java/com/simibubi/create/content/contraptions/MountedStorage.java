package com.simibubi.create.content.contraptions;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.sync.ContraptionItemPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class MountedStorage {

	private static final ItemStackHandler dummyHandler = new ItemStackHandler();

	ItemStackHandler handler;
	boolean noFuel;
	boolean valid;
	
	private int packetCooldown = 0;
	private boolean sendPacket = false;

	BlockEntity blockEntity;

	public static boolean canUseAsStorage(BlockEntity be) {
		if (be == null)
			return false;
		if (be instanceof MechanicalCrafterBlockEntity)
			return false;
		if (AllBlockEntityTypes.CREATIVE_CRATE.is(be))
			return true;
		if (be instanceof ShulkerBoxBlockEntity)
			return true;
		if (be instanceof ChestBlockEntity)
			return true;
		if (be instanceof BarrelBlockEntity)
			return true;
		if (be instanceof ItemVaultBlockEntity)
			return true;
		if (be instanceof DepotBlockEntity)
			return true;

		try {
			LazyOptional<IItemHandler> capability = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
			IItemHandler handler = capability.orElse(null);
			if (handler instanceof ItemStackHandler)
				return !(handler instanceof ProcessingInventory);
			return canUseModdedInventory(be, handler);

		} catch (Exception e) {
			return false;
		}
	}

	public static boolean canUseModdedInventory(BlockEntity be, IItemHandler handler) {
		if (!(handler instanceof IItemHandlerModifiable validItemHandler))
			return false;
		BlockState blockState = be.getBlockState();
		if (AllBlockTags.CONTRAPTION_INVENTORY_DENY.matches(blockState))
			return false;

		// There doesn't appear to be much of a standard for tagging chests/barrels
		String blockId = ForgeRegistries.BLOCKS.getKey(blockState.getBlock())
			.getPath();
		if (blockId.contains("ender"))
			return false;
		return blockId.endsWith("_chest") || blockId.endsWith("_barrel");
	}

	public MountedStorage(BlockEntity be) {
		this.blockEntity = be;
		handler = dummyHandler;
		noFuel = be instanceof ItemVaultBlockEntity;
	}

	public void removeStorageFromWorld() {
		valid = false;
		sendPacket = false;
		if (blockEntity == null)
			return;

		if (blockEntity instanceof DepotBlockEntity depot) {
			handler = new SyncedMountedItemStackHandler(1);
			handler.setStackInSlot(0, depot.getHeldItem());
			valid = true;
			return;
		}
		
		if (blockEntity instanceof ChestBlockEntity) {
			CompoundTag tag = blockEntity.saveWithFullMetadata();
			if (tag.contains("LootTable", 8))
				return;

			handler = new ItemStackHandler(((ChestBlockEntity) blockEntity).getContainerSize());
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(tag, items);
			for (int i = 0; i < items.size(); i++)
				handler.setStackInSlot(i, items.get(i));
			valid = true;
			return;
		}

		IItemHandler beHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
			.orElse(dummyHandler);
		if (beHandler == dummyHandler)
			return;

		// multiblock vaults need to provide individual invs
		if (blockEntity instanceof ItemVaultBlockEntity) {
			handler = ((ItemVaultBlockEntity) blockEntity).getInventoryOfBlock();
			valid = true;
			return;
		}

		// be uses ItemStackHandler
		if (beHandler instanceof ItemStackHandler) {
			handler = (ItemStackHandler) beHandler;
			valid = true;
			return;
		}

		// serialization not accessible -> fill into a serializable handler
		if (beHandler instanceof IItemHandlerModifiable) {
			IItemHandlerModifiable inv = (IItemHandlerModifiable) beHandler;
			handler = new ItemStackHandler(beHandler.getSlots());
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				handler.setStackInSlot(slot, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
			valid = true;
			return;
		}

	}

	public void addStorageToWorld(BlockEntity be) {
		// FIXME: More dynamic mounted storage in .4
		if (handler instanceof BottomlessItemHandler)
			return;

		if (be instanceof DepotBlockEntity depot) {
			if (handler.getSlots() > 0)
				depot.getBehaviour(DepotBehaviour.TYPE)
					.setCenteredHeldItem(new TransportedItemStack(handler.getStackInSlot(0)));
			return;
		}

		if (be instanceof ChestBlockEntity) {
			CompoundTag tag = be.saveWithFullMetadata();
			tag.remove("Items");
			NonNullList<ItemStack> items = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			for (int i = 0; i < items.size(); i++)
				items.set(i, handler.getStackInSlot(i));
			ContainerHelper.saveAllItems(tag, items);
			be.load(tag);
			return;
		}

		if (be instanceof ItemVaultBlockEntity) {
			((ItemVaultBlockEntity) be).applyInventoryToBlock(handler);
			return;
		}

		LazyOptional<IItemHandler> capability = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
		IItemHandler teHandler = capability.orElse(null);
		if (!(teHandler instanceof IItemHandlerModifiable))
			return;

		IItemHandlerModifiable inv = (IItemHandlerModifiable) teHandler;
		for (int slot = 0; slot < Math.min(inv.getSlots(), handler.getSlots()); slot++)
			inv.setStackInSlot(slot, handler.getStackInSlot(slot));
	}

	public void tick(Entity entity, BlockPos pos, boolean isRemote) {
		if (isRemote)
			return;
		if (packetCooldown > 0) {
			packetCooldown--;
			return;
		}
		if (sendPacket) {
			sendPacket = false;
			AllPackets.getChannel()
				.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
					new ContraptionItemPacket(entity.getId(), pos, handler));
			packetCooldown = 8;
		}
	}

	public void updateItems(List<ItemStack> containedItems) {
		for (int i = 0; i < Math.min(containedItems.size(), handler.getSlots()); i++)
			handler.setStackInSlot(i, containedItems.get(i));
		if (blockEntity instanceof DepotBlockEntity depot)
			depot.setHeldItem(handler.getStackInSlot(0));
	}
	
	public IItemHandlerModifiable getItemHandler() {
		return handler;
	}

	public CompoundTag serialize() {
		if (!valid)
			return null;

		CompoundTag tag = handler.serializeNBT();
		if (noFuel)
			NBTHelper.putMarker(tag, "NoFuel");
		if (handler instanceof ToolboxInventory)
			NBTHelper.putMarker(tag, "Toolbox");
		if (needsSync())
			NBTHelper.putMarker(tag, "Synced");
			
		if (!(handler instanceof BottomlessItemHandler))
			return tag;

		NBTHelper.putMarker(tag, "Bottomless");
		tag.put("ProvidedStack", handler.getStackInSlot(0)
			.serializeNBT());
		return tag;
	}

	public static MountedStorage deserialize(CompoundTag nbt) {
		MountedStorage storage = new MountedStorage(null);
		storage.handler = new ItemStackHandler();
		if (nbt == null)
			return storage;
		if (nbt.contains("Toolbox"))
			storage.handler = new ToolboxInventory(null);
		if (nbt.contains("Synced"))
			storage.handler = storage.new SyncedMountedItemStackHandler(1);
		
		storage.valid = true;
		storage.noFuel = nbt.contains("NoFuel");

		if (nbt.contains("Bottomless")) {
			ItemStack providedStack = ItemStack.of(nbt.getCompound("ProvidedStack"));
			storage.handler = new BottomlessItemHandler(() -> providedStack);
			return storage;
		}

		storage.handler.deserializeNBT(nbt);
		return storage;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean canUseForFuel() {
		return !noFuel;
	}
	
	public boolean needsSync() {
		return handler instanceof SyncedMountedItemStackHandler;
	}
	
	public class SyncedMountedItemStackHandler extends ItemStackHandler {
		
		public SyncedMountedItemStackHandler(int i) {
			super(i);
		}

		@Override
		protected void onContentsChanged(int slot) {
			sendPacket = true;
		}
		
	}

}
