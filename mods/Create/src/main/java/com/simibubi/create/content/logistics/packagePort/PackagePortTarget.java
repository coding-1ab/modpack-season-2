package com.simibubi.create.content.logistics.packagePort;

import java.util.Map;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftBlockEntity;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftBlockEntity.ConnectedPort;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftBlockEntity.ConnectionStats;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftPackage;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.filter.FilterItemStack.PackageFilterItemStack;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class PackagePortTarget {

	public BlockPos relativePos;
	private String typeKey;

	public PackagePortTarget(String typeKey, BlockPos relativePos) {
		this.typeKey = typeKey;
		this.relativePos = relativePos;
	}

	public abstract boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate);

	public void setup(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {}

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		writeInternal(compoundTag);
		compoundTag.putString("Type", typeKey);
		compoundTag.put("RelativePos", NbtUtils.writeBlockPos(relativePos));
		return compoundTag;
	}

	public static PackagePortTarget read(CompoundTag tag) {
		if (tag.isEmpty())
			return null;

		BlockPos relativePos = NbtUtils.readBlockPos(tag.getCompound("RelativePos"));
		PackagePortTarget target = switch (tag.getString("Type")) {

		case "ChainLift" -> new ChainLiftPortTarget(relativePos, 0, null);

		default -> null;
		};
		target.readInternal(tag);
		return target;
	}

	protected abstract void writeInternal(CompoundTag tag);

	protected abstract void readInternal(CompoundTag tag);

	protected BlockEntity be(LevelAccessor level, BlockPos portPos) {
		return level.getBlockEntity(portPos.offset(relativePos));
	}

	public static class ChainLiftPortTarget extends PackagePortTarget {

		public float chainPos;
		public BlockPos connection;
		public boolean flipped;

		public ChainLiftPortTarget(BlockPos relativePos, float chainPos, @Nullable BlockPos connection) {
			super("ChainLift", relativePos);
			this.chainPos = chainPos;
			this.connection = connection;
		}

		@Override
		public void setup(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (be(level, portPos) instanceof ChainLiftBlockEntity clbe)
				flipped = clbe.getSpeed() < 0;
		}

		@Override
		public boolean export(LevelAccessor level, BlockPos portPos, ItemStack box, boolean simulate) {
			if (!(be(level, portPos) instanceof ChainLiftBlockEntity clbe))
				return false;
			if (simulate)
				return true;
			ChainLiftPackage box2 = new ChainLiftPackage(chainPos, box.copy());
			if (connection == null)
				return clbe.addLoopingPackage(box2);
			return clbe.addTravellingPackage(box2, connection);
		}

		@Override
		public void register(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (!(be(level, portPos) instanceof ChainLiftBlockEntity clbe))
				return;
			ChainLiftBlockEntity actualBe = clbe;

			// Jump to opposite chain if motion reversed
			if (connection != null && clbe.getSpeed() < 0 != flipped) {
				deregister(ppbe, level, portPos);
				actualBe = AllBlocks.CHAIN_LIFT.get()
					.getBlockEntity(level, clbe.getBlockPos()
						.offset(connection));
				if (actualBe == null)
					return;
				clbe.prepareStats();
				ConnectionStats stats = clbe.connectionStats.get(connection);
				if (stats != null)
					chainPos = stats.chainLength() - chainPos;
				connection = connection.multiply(-1);
				flipped = !flipped;
				relativePos = actualBe.getBlockPos()
					.subtract(portPos);
				ppbe.notifyUpdate();
			}

			if (connection != null && !actualBe.connections.contains(connection))
				return;
			String portFilter = getFilterString(ppbe);
			if (portFilter == null)
				return;
			actualBe.routingTable.receivePortInfo(portFilter, connection == null ? BlockPos.ZERO : connection);
			Map<BlockPos, ConnectedPort> portMap = connection == null ? actualBe.loopPorts : actualBe.travelPorts;
			portMap.put(relativePos.multiply(-1), new ConnectedPort(chainPos, connection, portFilter));
		}

		@Override
		public void deregister(PackagePortBlockEntity ppbe, LevelAccessor level, BlockPos portPos) {
			if (!(be(level, portPos) instanceof ChainLiftBlockEntity clbe))
				return;
			clbe.loopPorts.remove(relativePos.multiply(-1));
			clbe.travelPorts.remove(relativePos.multiply(-1));
			String portFilter = getFilterString(ppbe);
			if (portFilter == null)
				return;
			clbe.routingTable.entriesByDistance.removeIf(e -> e.endOfRoute() && e.port()
				.equals(portFilter));
			clbe.routingTable.changed = true;
		}

		private String getFilterString(PackagePortBlockEntity ppbe) {
			ItemStack filter = ppbe.filtering.getFilter();
			String portFilter = null;
			FilterItemStack filterStack = FilterItemStack.of(filter);
			if (AllItems.PACKAGE_FILTER.isIn(filter)) {
				if (!(filterStack instanceof PackageFilterItemStack pfis))
					return "";
				portFilter = pfis.filterString;
			}
			return portFilter;
		}

		@Override
		protected void writeInternal(CompoundTag tag) {
			tag.putFloat("ChainPos", chainPos);
			if (connection != null) {
				tag.put("Connection", NbtUtils.writeBlockPos(connection));
				tag.putBoolean("Flipped", flipped);
			}
		}

		@Override
		protected void readInternal(CompoundTag tag) {
			chainPos = tag.getFloat("ChainPos");
			if (tag.contains("Connection")) {
				connection = NbtUtils.readBlockPos(tag.getCompound("Connection"));
				flipped = tag.getBoolean("Flipped");
			}
		}

	}

}
