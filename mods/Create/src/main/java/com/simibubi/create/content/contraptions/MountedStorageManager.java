package com.simibubi.create.content.contraptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class MountedStorageManager {

	private ImmutableMap.Builder<BlockPos, MountedItemStorage> itemsBuilder;

	protected ImmutableMap<BlockPos, MountedItemStorage> allItemStorages;

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
		this.externalHandlers = new ArrayList<>();
		this.allItems = null;
		this.itemsBuilder = ImmutableMap.builder();
	}

	public void addBlock(Level level, BlockState state, BlockPos globalPos, BlockPos localPos, @Nullable BlockEntity be) {
		MountedItemStorageType<?> type = MountedStorageTypeRegistry.ITEM_LOOKUP.find(state);
		if (type == null)
			return;

		MountedItemStorage storage = type.mount(level, state, globalPos, be);
		if (storage != null) {
			this.itemsBuilder.put(localPos, storage);
		}
	}

	public void unmount(Level level, StructureBlockInfo info, BlockPos globalPos, @Nullable BlockEntity be) {
		BlockPos localPos = info.pos();
		MountedItemStorage storage = this.getAllItemStorages().get(localPos);
		if (storage != null) {
			BlockState state = info.state();
			MountedItemStorageType<?> expectedType = MountedStorageTypeRegistry.ITEM_LOOKUP.find(state);
			if (storage.type == expectedType) {
				storage.unmount(level, state, globalPos, be);
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
		ListTag items = new ListTag();
		nbt.put("items", items);

		this.getAllItemStorages().forEach(
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

	public IFluidHandler getFluids() {
		return new CombinedTankWrapper();
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
