package com.simibubi.create.content.contraptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.simibubi.create.content.equipment.toolbox.ToolboxMountedStorage;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorage;
import com.simibubi.create.content.fluids.tank.storage.creative.CreativeFluidTankMountedStorage;
import com.simibubi.create.content.logistics.crate.CreativeCrateMountedStorage;
import com.simibubi.create.content.logistics.vault.ItemVaultMountedStorage;
import com.simibubi.create.impl.contraption.storage.FallbackMountedStorage;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class MountedStorageManager {
	// builders used during assembly, null afterward
	private ImmutableMap.Builder<BlockPos, MountedItemStorage> itemsBuilder;
	private ImmutableMap.Builder<BlockPos, MountedFluidStorage> fluidsBuilder;

	// built data structures after assembly, null before
	protected ImmutableMap<BlockPos, MountedItemStorage> allItemStorages;
	// different from allItemStorages, does not contain internal ones
	protected MountedItemStorageWrapper items;
	@Nullable
	protected MountedItemStorageWrapper fuelItems;
	protected MountedFluidStorageWrapper fluids;

	private List<IItemHandlerModifiable> externalHandlers;
	protected CombinedInvWrapper allItems;

	public MountedStorageManager() {
		this.reset();
	}

	public void initialize() {
		if (this.isInitialized()) {
			throw new IllegalStateException("Mounted storage has already been initialized");
		}

		this.allItemStorages = this.itemsBuilder.build();

		this.items = new MountedItemStorageWrapper(subMap(
			this.allItemStorages, storage -> !storage.isInternal()
		));

		this.allItems = this.items;
		this.itemsBuilder = null;

		ImmutableMap<BlockPos, MountedItemStorage> fuelMap = subMap(
			this.allItemStorages, storage -> !storage.isInternal() && storage.providesFuel()
		);
		this.fuelItems = fuelMap.isEmpty() ? null : new MountedItemStorageWrapper(fuelMap);

		ImmutableMap<BlockPos, MountedFluidStorage> fluids = this.fluidsBuilder.build();
		this.fluids = new MountedFluidStorageWrapper(fluids);
		this.fluidsBuilder = null;
	}

	private boolean isInitialized() {
		return this.itemsBuilder == null;
	}

	private void assertInitialized() {
		if (!this.isInitialized()) {
			throw new IllegalStateException("MountedStorageManager is uninitialized");
		}
	}

	protected void reset() {
		this.allItemStorages = null;
		this.items = null;
		this.fuelItems = null;
		this.fluids = null;
		this.externalHandlers = new ArrayList<>();
		this.allItems = null;
		this.itemsBuilder = ImmutableMap.builder();
		this.fluidsBuilder = ImmutableMap.builder();
	}

	public void addBlock(Level level, BlockState state, BlockPos globalPos, BlockPos localPos, @Nullable BlockEntity be) {
		MountedItemStorageType<?> itemType = MountedStorageTypeRegistry.ITEM_LOOKUP.find(state);
		if (itemType != null) {
			MountedItemStorage storage = itemType.mount(level, state, globalPos, be);
			if (storage != null) {
				this.itemsBuilder.put(localPos, storage);
			}
		}

		MountedFluidStorageType<?> fluidType = MountedStorageTypeRegistry.FLUID_LOOKUP.find(state);
		if (fluidType != null) {
			MountedFluidStorage storage = fluidType.mount(level, state, globalPos, be);
			if (storage != null) {
				this.fluidsBuilder.put(localPos, storage);
			}
		}
	}

	public void unmount(Level level, StructureBlockInfo info, BlockPos globalPos, @Nullable BlockEntity be) {
		BlockPos localPos = info.pos();
		BlockState state = info.state();

		MountedItemStorage itemStorage = this.getAllItemStorages().get(localPos);
		if (itemStorage != null) {
			MountedItemStorageType<?> expectedType = MountedStorageTypeRegistry.ITEM_LOOKUP.find(state);
			if (itemStorage.type == expectedType) {
				itemStorage.unmount(level, state, globalPos, be);
			}
		}

		MountedFluidStorage fluidStorage = this.getFluids().storages.get(localPos);
		if (fluidStorage != null) {
			MountedFluidStorageType<?> expectedType = MountedStorageTypeRegistry.FLUID_LOOKUP.find(state);
			if (fluidStorage.type == expectedType) {
				fluidStorage.unmount(level, state, globalPos, be);
			}
		}
	}

	public void read(CompoundTag nbt) {
		this.reset();

		try {
			NBTHelper.iterateCompoundList(nbt.getList("items", Tag.TAG_COMPOUND), tag -> {
				BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
				CompoundTag data = tag.getCompound("storage");
				MountedItemStorage.CODEC.decode(NbtOps.INSTANCE, data)
					.result()
					.map(Pair::getFirst)
					.ifPresent(storage -> this.itemsBuilder.put(pos, storage));
			});

			NBTHelper.iterateCompoundList(nbt.getList("fluids", Tag.TAG_COMPOUND), tag -> {
				BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
				CompoundTag data = tag.getCompound("storage");
				MountedFluidStorage.CODEC.decode(NbtOps.INSTANCE, data)
					.result()
					.map(Pair::getFirst)
					.ifPresent(storage -> this.fluidsBuilder.put(pos, storage));
			});

			this.readLegacy(nbt);
		} catch (Throwable t) {
			Create.LOGGER.error("Error deserializing mounted storage", t);
			// an exception will leave the manager in an invalid state, initialize must be called
		}

		this.initialize();
	}

	public void write(CompoundTag nbt, boolean clientPacket) {
		if (!this.getAllItemStorages().isEmpty()) {
			ListTag items = new ListTag();
			this.getAllItemStorages().forEach(
				(pos, storage) -> MountedItemStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage)
					.result().ifPresent(encoded -> {
						CompoundTag tag = new CompoundTag();
						tag.put("pos", NbtUtils.writeBlockPos(pos));
						tag.put("storage", encoded);
						items.add(tag);
					})
			);
			nbt.put("items", items);
		}

		if (!this.getFluids().storages.isEmpty()) {
			ListTag fluids = new ListTag();
			this.getFluids().storages.forEach(
				(pos, storage) -> MountedFluidStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage)
					.result().ifPresent(encoded -> {
						CompoundTag tag = new CompoundTag();
						tag.put("pos", NbtUtils.writeBlockPos(pos));
						tag.put("storage", encoded);
						fluids.add(tag);
					})
			);
			nbt.put("fluids", fluids);
		}
	}

	public void attachExternal(IItemHandlerModifiable externalStorage) {
		this.externalHandlers.add(externalStorage);
		IItemHandlerModifiable[] all = new IItemHandlerModifiable[this.externalHandlers.size() + 1];
		all[0] = this.items;
		for (int i = 0; i < this.externalHandlers.size(); i++) {
			all[i + 1] = this.externalHandlers.get(i);
		}

		this.allItems = new CombinedInvWrapper(all);
	}

	/**
	 * Gets a map of all MountedItemStorages in the contraption, irrelevant of them
	 * being internal or providing fuel. The methods below are likely more useful.
	 * @see MountedItemStorage#isInternal()
	 * @see MountedItemStorage#providesFuel()
	 */
	public ImmutableMap<BlockPos, MountedItemStorage> getAllItemStorages() {
		this.assertInitialized();
		return this.allItemStorages;
	}

	/**
	 * Gets an item handler wrapping all non-internal mounted storages. This is not
	 * the whole contraption inventory as it does not include external storages.
	 * Most often, you want {@link #getAllItems()}, which does.
	 */
	public MountedItemStorageWrapper getMountedItems() {
		this.assertInitialized();
		return this.items;
	}

	/**
	 * Gets an item handler wrapping all non-internal mounted storages that provide fuel.
	 * May be null if none are present.
	 */
	@Nullable
	public MountedItemStorageWrapper getFuelItems() {
		this.assertInitialized();
		return this.fuelItems;
	}

	/**
	 * Gets an item handler wrapping all non-internal mounted storages and all external storages.
	 * Non-internal storages are mounted storages that are intended to be exposed to the entire
	 * contraption. External storages are non-mounted storages that are still part of a contraption's
	 * inventory, such as the inventories of chest minecarts.
	 */
	public CombinedInvWrapper getAllItems() {
		this.assertInitialized();
		return this.allItems;
	}

	public MountedFluidStorageWrapper getFluids() {
		this.assertInitialized();
		return this.fluids;
	}

	public boolean handlePlayerStorageInteraction(Contraption contraption, Player player, BlockPos localPos) {
		StructureBlockInfo info = contraption.getBlocks().get(localPos);
		if (info == null)
			return false;

		MountedStorageManager storageManager = contraption.getStorageForSpawnPacket();
		MountedItemStorage storage = storageManager.getAllItemStorages().get(localPos);

		if (storage != null) {
			return !(player instanceof ServerPlayer serverPlayer) || storage.handleInteraction(serverPlayer, contraption, info);
		} else {
			return false;
		}
	}

	private void readLegacy(CompoundTag nbt) {
		NBTHelper.iterateCompoundList(nbt.getList("Storage", Tag.TAG_COMPOUND), tag -> {
			BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("Pos"));
			CompoundTag data = tag.getCompound("Data");

			// note: Synced is ignored, since all are synced
			if (data.contains("Toolbox")) {
				this.itemsBuilder.put(pos, ToolboxMountedStorage.fromLegacy(data));
			} else if (data.contains("NoFuel")) {
				this.itemsBuilder.put(pos, ItemVaultMountedStorage.fromLegacy(data));
			} else if (data.contains("Bottomless")) {
				ItemStack supplied = ItemStack.of(data.getCompound("ProvidedStack"));
				this.itemsBuilder.put(pos, new CreativeCrateMountedStorage(supplied));
			} else {
				// we can create a fallback storage safely, it will be validated before unmounting
				ItemStackHandler handler = new ItemStackHandler();
				handler.deserializeNBT(data);
				this.itemsBuilder.put(pos, new FallbackMountedStorage(handler));
			}
		});

		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", Tag.TAG_COMPOUND), tag -> {
			BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("Pos"));
			CompoundTag data = tag.getCompound("Data");

			if (data.contains("Bottomless")) {
				this.fluidsBuilder.put(pos, CreativeFluidTankMountedStorage.fromLegacy(data));
			} else {
				this.fluidsBuilder.put(pos, FluidTankMountedStorage.fromLegacy(data));
			}
		});
	}

	private static <K, V> ImmutableMap<K, V> subMap(Map<K, V> map, Predicate<V> predicate) {
		ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
		map.forEach((key, value) -> {
			if (predicate.test(value)) {
				builder.put(key, value);
			}
		});
		return builder.build();
	}
}
