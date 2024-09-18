package com.simibubi.create.content.trains.station;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;

public class GlobalStation extends SingleBlockEntityEdgePoint {

	public String name;
	public WeakReference<Train> nearestTrain;
	public boolean assembling;

	public Map<BlockPos, GlobalPackagePort> connectedPorts;

	public GlobalStation() {
		name = "Track Station";
		nearestTrain = new WeakReference<Train>(null);
		connectedPorts = new HashMap<>();
	}

	@Override
	public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
		super.blockEntityAdded(blockEntity, front);
		BlockState state = blockEntity.getBlockState();
		assembling =
			state != null && state.hasProperty(StationBlock.ASSEMBLING) && state.getValue(StationBlock.ASSEMBLING);
	}

	@Override
	public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
		super.read(nbt, migration, dimensions);
		name = nbt.getString("Name");
		assembling = nbt.getBoolean("Assembling");
		nearestTrain = new WeakReference<Train>(null);

		connectedPorts.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Ports", Tag.TAG_LIST), c -> {
			GlobalPackagePort port = new GlobalPackagePort();
			port.address = c.getString("Address");
			port.inBuffer = NBTHelper.readItemList(c.getList("InBuffer", Tag.TAG_LIST));
			connectedPorts.put(NbtUtils.readBlockPos(c.getCompound("Pos")), port);
		});
	}

	@Override
	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		name = buffer.readUtf();
		assembling = buffer.readBoolean();
		if (buffer.readBoolean())
			blockEntityPos = buffer.readBlockPos();
	}

	@Override
	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		super.write(nbt, dimensions);
		nbt.putString("Name", name);
		nbt.putBoolean("Assembling", assembling);

		nbt.put("Ports", NBTHelper.writeCompoundList(connectedPorts.entrySet(), e -> {
			CompoundTag c = new CompoundTag();
			c.putString("Address", e.getValue().address);
			c.put("InBuffer", NBTHelper.writeItemList(e.getValue().inBuffer));
			c.put("Pos", NbtUtils.writeBlockPos(e.getKey()));
			return c;
		}));
	}

	@Override
	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		buffer.writeUtf(name);
		buffer.writeBoolean(assembling);
		buffer.writeBoolean(blockEntityPos != null);
		if (blockEntityPos != null)
			buffer.writeBlockPos(blockEntityPos);
	}

	public boolean canApproachFrom(TrackNode side) {
		return isPrimary(side) && !assembling;
	}

	@Override
	public boolean canNavigateVia(TrackNode side) {
		return super.canNavigateVia(side) && !assembling;
	}

	public void reserveFor(Train train) {
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null
			|| nearestTrain.navigation.distanceToDestination > train.navigation.distanceToDestination)
			this.nearestTrain = new WeakReference<>(train);
	}

	public void cancelReservation(Train train) {
		if (nearestTrain.get() == train)
			nearestTrain = new WeakReference<>(null);
	}

	public void trainDeparted(Train train) {
		cancelReservation(train);
	}

	@Nullable
	public Train getPresentTrain() {
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null || nearestTrain.getCurrentStation() != this)
			return null;
		return nearestTrain;
	}

	@Nullable
	public Train getImminentTrain() {
		Train nearestTrain = getNearestTrain();
		if (nearestTrain == null)
			return nearestTrain;
		if (nearestTrain.getCurrentStation() == this)
			return nearestTrain;
		if (!nearestTrain.navigation.isActive())
			return null;
		if (nearestTrain.navigation.distanceToDestination > 30)
			return null;
		return nearestTrain;
	}

	@Nullable
	public Train getNearestTrain() {
		return this.nearestTrain.get();
	}

	// Package Port integration
	public static class GlobalPackagePort {
		public String address = "";
		public List<ItemStack> inBuffer = new ArrayList<>();
	}

	@Override
	public void tick(TrackGraph graph, boolean preTrains) {
		super.tick(graph, preTrains);
		if (preTrains)
			return;
		Train train = getPresentTrain();
		if (train == null || connectedPorts.isEmpty())
			return;

		Level level = null;

		for (Carriage carriage : train.carriages) {
			if (level == null) {
				CarriageContraptionEntity entity = carriage.anyAvailableEntity();
				if (entity != null && entity.level() instanceof ServerLevel sl)
					level = sl.getServer()
						.getLevel(getBlockEntityDimension());
			}

			IItemHandlerModifiable inventory = carriage.storage.getItems();
			if (inventory == null)
				continue;

			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemStack stack = inventory.getStackInSlot(slot);
				if (!PackageItem.isPackage(stack))
					continue;
				for (Entry<BlockPos, GlobalPackagePort> entry : connectedPorts.entrySet()) {
					GlobalPackagePort port = entry.getValue();
					BlockPos pos = entry.getKey();
					
					if (!PackageItem.matchAddress(stack, port.address))
						continue;
					
					if (level != null && level.isLoaded(pos) && level.getBlockEntity(pos) instanceof PackagePortBlockEntity ppbe) {
						if (ppbe.isAnimationInProgress())
							continue;
						if (ppbe.inventory.isBackedUp())
							continue;
						ppbe.startAnimation(stack, false);
						
					} else {
						if (port.inBuffer.size() >= 18)
							continue;
						port.inBuffer.add(stack);
						Create.RAILWAYS.markTracksDirty();
					}
					
					inventory.setStackInSlot(slot, ItemStack.EMPTY);
					break;
				}
			}

		}
	}

}
