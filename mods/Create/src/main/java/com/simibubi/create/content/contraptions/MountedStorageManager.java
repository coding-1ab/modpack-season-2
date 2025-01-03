package com.simibubi.create.content.contraptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistry;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;

import com.simibubi.create.foundation.fluid.CombinedTankWrapper;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.Nullable;

public class MountedStorageManager {

	private ImmutableMap.Builder<BlockPos, MountedItemStorage> itemsBuilder;

	protected MountedItemStorageWrapper items;
	@Nullable
	protected MountedItemStorageWrapper fuelItems;

	private List<IItemHandlerModifiable> externalHandlers;
	protected CombinedInvWrapper allItems;

	public MountedStorageManager() {
		this.reset();
	}

	public void initialize() {
		if (this.isInitialized()) {
			throw new IllegalStateException("Mounted storage has already been initialized");
		}

		this.items = new MountedItemStorageWrapper(this.itemsBuilder.build());
		this.allItems = this.items;
		this.itemsBuilder = null;

		ImmutableMap.Builder<BlockPos, MountedItemStorage> fuel = ImmutableMap.builder();
		this.items.storages.forEach((pos, storage) -> {
			if (storage.providesFuel()) {
				fuel.put(pos, storage);
			}
		});
		ImmutableMap<BlockPos, MountedItemStorage> fuelMap = fuel.build();
		this.fuelItems = fuelMap.isEmpty() ? null : new MountedItemStorageWrapper(fuelMap);
	}

	private boolean isInitialized() {
		return this.itemsBuilder == null;
	}

	protected void reset() {
		this.items = null;
		this.fuelItems = null;
		this.externalHandlers = new ArrayList<>();
		this.allItems = null;
		this.itemsBuilder = ImmutableMap.builder();
	}

	public void addBlock(Level level, BlockState state, BlockPos globalPos, BlockPos localPos, @Nullable BlockEntity be) {
		MountedItemStorageType<?> type = getMountedStorageType(state);
		if (type == null)
			return;

		MountedItemStorage storage = type.mount(level, state, globalPos, be);
		if (storage != null) {
			this.itemsBuilder.put(localPos, storage);
		}
	}

	public void unmount(Level level, StructureBlockInfo info, BlockPos globalPos, @Nullable BlockEntity be) {
		BlockPos localPos = info.pos();
		MountedItemStorage storage = this.items.storages.get(localPos);
		if (storage != null) {
			Block block = info.state().getBlock();
			MountedItemStorageType<?> expectedType = MountedStorageTypeRegistry.ITEMS_BY_BLOCK.get(block);
			if (typeMatches(expectedType, storage, info, be)) {
				storage.unmount(level, info.state(), globalPos, be);
			}
		}
	}

	public void read(CompoundTag nbt) {
		this.reset();

		NBTHelper.iterateCompoundList(nbt.getList("items", Tag.TAG_COMPOUND), tag -> {
			BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
			CompoundTag data = tag.getCompound("storage");
			MountedItemStorage.CODEC.decode(NbtOps.INSTANCE, data)
				.result()
				.map(Pair::getFirst)
//				.or(() -> Optional.ofNullable(parseLegacy(data)))
				.ifPresent(storage -> this.itemsBuilder.put(pos, storage));
		});

//		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", Tag.TAG_COMPOUND), c -> fluidStorage
//			.put(NbtUtils.readBlockPos(c.getCompound("Pos")), MountedFluidStorage.deserialize(c.getCompound("Data"))));

		this.initialize();
	}

	public void write(CompoundTag nbt, boolean clientPacket) {
		if (clientPacket)
			return;

		ListTag items = new ListTag();
		nbt.put("items", items);

		this.items.storages.forEach(
			(pos, storage) -> MountedItemStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage)
				.result().ifPresent(encoded -> {
					CompoundTag tag = new CompoundTag();
					tag.put("pos", NbtUtils.writeBlockPos(pos));
					tag.put("storage", encoded);
					items.add(tag);
				})
		);
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
	 * @return the item handlers for all mounted storages
	 */
	public MountedItemStorageWrapper getMountedItems() {
		return Objects.requireNonNull(this.items, "Cannot get items for uninitialized manager");
	}

	/**
	 * @return the item handlers for all mounted storages that may contain fuel, or null if none support it
	 */
	@Nullable
	public MountedItemStorageWrapper getFuelItems() {
		return Objects.requireNonNull(this.fuelItems, "Cannot get fuelItems for uninitialized manager");
	}

	/**
	 * @return the item handler representing all mounted storage and all external storage
	 */
	public CombinedInvWrapper getAllItems() {
		return Objects.requireNonNull(this.allItems, "Cannot get allItems for uninitialized manager");
	}

	public IFluidHandler getFluids() {
		return new CombinedTankWrapper();
	}

	public boolean handlePlayerStorageInteraction(Contraption contraption, Player player, BlockPos localPos) {
		StructureBlockInfo info = contraption.getBlocks().get(localPos);
		if (info == null)
			return false;

		if (player.level().isClientSide()) {
			return getMountedStorageType(info.state()) != null;
		}

		MountedStorageManager storageManager = contraption.getStorageForSpawnPacket();
		MountedItemStorage storage = storageManager.items.storages.get(localPos);
		if (storage != null) {
			return storage.handleInteraction(player, contraption, info);
		} else {
			return false;
		}
	}

	private static boolean typeMatches(MountedItemStorageType<?> registered, MountedItemStorage storage,
									   StructureBlockInfo info, @Nullable BlockEntity be) {
		MountedItemStorageType<?> actual = storage.type;
		if (registered == actual)
			return true;
		if (registered != null)
			return false;

		BlockState state = info.state();
		if (actual == AllMountedStorageTypes.SIMPLE.get()) {
			return AllTags.AllBlockTags.SIMPLE_MOUNTED_STORAGE.matches(state);
		} else if (actual == AllMountedStorageTypes.FALLBACK.get() && be != null) {
			return !AllTags.AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST.matches(state)
				&& be.getCapability(ForgeCapabilities.ITEM_HANDLER)
				.resolve()
				.flatMap(AllMountedStorageTypes.FALLBACK.get()::validate)
				.isPresent();
		} else {
			return false;
		}
	}

	@Nullable
	private static MountedItemStorageType<?> getMountedStorageType(BlockState state) {
		MountedItemStorageType<?> registered = MountedStorageTypeRegistry.ITEMS_BY_BLOCK.get(state.getBlock());
		if (registered != null)
			return registered;

		if (AllTags.AllBlockTags.SIMPLE_MOUNTED_STORAGE.matches(state)) {
			return AllMountedStorageTypes.SIMPLE.get();
		} else if (!AllTags.AllBlockTags.FALLBACK_MOUNTED_STORAGE_BLACKLIST.matches(state)) {
			return AllMountedStorageTypes.FALLBACK.get();
		} else {
			return null;
		}
	}
}
