package com.simibubi.create.content.kinetics.chainLift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftPackage.ChainLiftPackagePhysicsData;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftShape.ChainLiftBB;
import com.simibubi.create.content.kinetics.chainLift.ChainLiftShape.ChainLiftOBB;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

public class ChainLiftBlockEntity extends KineticBlockEntity {

	public record ConnectionStats(float tangentAngle, float chainLength, Vec3 start, Vec3 end) {
	}

	public record ConnectedPort(float chainPosition, BlockPos connection, String filter) {
	}

	public Set<BlockPos> connections = new HashSet<>();
	public Map<BlockPos, ConnectionStats> connectionStats;

	public Map<BlockPos, ConnectedPort> loopPorts = new HashMap<>();
	public Map<BlockPos, ConnectedPort> travelPorts = new HashMap<>();
	public ChainLiftRoutingTable routingTable = new ChainLiftRoutingTable();
	
	List<ChainLiftPackage> loopingPackages = new ArrayList<>();
	Map<BlockPos, List<ChainLiftPackage>> travellingPackages = new HashMap<>();
	
	public boolean reversed;

	public ChainLiftBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		}

	@Override
	protected AABB createRenderBoundingBox() {
		return INFINITE_EXTENT_AABB; // TODO: compute smallest possible from connection data
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateChainShapes();
	}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToTooltip(tooltip, isPlayerSneaking);
		tooltip.addAll(routingTable.createSummary());
		if (!loopPorts.isEmpty())
			tooltip.add(Components.literal(loopPorts.size() + " Loop ports"));
		if (!travelPorts.isEmpty())
			tooltip.add(Components.literal(travelPorts.size() + " Travel ports"));
		return true;
	}

	@Override
	public void tick() {
		super.tick();

		float serverSpeed = ServerSpeedProvider.get();
		float speed = getSpeed() / 180f;
		float radius = 1.5f;
		float distancePerTick = Math.abs(speed);
		float degreesPerTick = (speed / (Mth.PI * radius)) * 360f;
		boolean reversedPreviously = reversed;

		prepareStats();

		if (level.isClientSide()) {
			for (ChainLiftPackage box : loopingPackages)
				tickBoxVisuals(box);
			for (Entry<BlockPos, List<ChainLiftPackage>> entry : travellingPackages.entrySet())
				for (ChainLiftPackage box : entry.getValue())
					tickBoxVisuals(box);
		}

		/* if serverside */ {
			routingTable.tick();
			if (routingTable.shouldAdvertise()) {
				if (level.isClientSide())
					level.addParticle(ParticleTypes.CRIT, worldPosition.getX() + .5, worldPosition.getY() + 1.5,
						worldPosition.getZ() + .5, 0, 0, 0);
				for (BlockPos pos : connections)
					if (level.getBlockEntity(worldPosition.offset(pos)) instanceof ChainLiftBlockEntity clbe)
						routingTable.advertiseTo(pos, clbe.routingTable);
				routingTable.changed = false;
				routingTable.lastUpdate = 0;
			}
		}

		if (speed == 0) {
			updateBoxWorldPositions();
			return;
		}

		if (reversedPreviously != reversed) {
			for (Entry<BlockPos, List<ChainLiftPackage>> entry : travellingPackages.entrySet()) {
				BlockPos offset = entry.getKey();
				if (!(level.getBlockEntity(worldPosition.offset(offset)) instanceof ChainLiftBlockEntity otherLift))
					continue;
				for (Iterator<ChainLiftPackage> iterator = entry.getValue()
					.iterator(); iterator.hasNext();) {
					ChainLiftPackage box = iterator.next();
					if (box.justFlipped)
						continue;
					box.justFlipped = true;
					float length = (float) Vec3.atLowerCornerOf(offset)
						.length() - 22 / 16f;
					box.chainPosition = length - box.chainPosition;
					otherLift.addTravellingPackage(box, offset.multiply(-1));
					iterator.remove();
				}
			}
			notifyUpdate();
		}

		for (Entry<BlockPos, List<ChainLiftPackage>> entry : travellingPackages.entrySet()) {
			BlockPos target = entry.getKey();
			ConnectionStats stats = connectionStats.get(target);

			Travelling: for (Iterator<ChainLiftPackage> iterator = entry.getValue()
				.iterator(); iterator.hasNext();) {
				ChainLiftPackage box = iterator.next();

				float prevChainPosition = box.chainPosition;
				box.chainPosition += serverSpeed * distancePerTick;
				box.chainPosition = Math.min(stats.chainLength, box.chainPosition);
				box.justFlipped = false;

				if (level.isClientSide())
					continue;

				for (Entry<BlockPos, ConnectedPort> portEntry : travelPorts.entrySet()) {
					ConnectedPort port = portEntry.getValue();
					float chainPosition = port.chainPosition();

					if (prevChainPosition > chainPosition || box.chainPosition < chainPosition)
						continue;
					if (!PackageItem.matchAddress(box.item, port.filter()))
						continue;
					if (!exportToPort(box, portEntry.getKey()))
						continue;

					iterator.remove();
					notifyUpdate();
					continue Travelling;
				}

				if (box.chainPosition < stats.chainLength)
					continue;

				// transfer to other
				if (level.getBlockEntity(worldPosition.offset(target)) instanceof ChainLiftBlockEntity clbe) {
					box.chainPosition = wrapAngle(stats.tangentAngle + 180 + 2 * 49 * (reversed ? -1 : 1));
					clbe.addLoopingPackage(box);
					iterator.remove();
					notifyUpdate();
				}
			}
		}

		Looping: for (Iterator<ChainLiftPackage> iterator = loopingPackages.iterator(); iterator.hasNext();) {
			ChainLiftPackage box = iterator.next();

			float prevChainPosition = box.chainPosition;
			box.chainPosition += serverSpeed * degreesPerTick;
			box.chainPosition = wrapAngle(box.chainPosition);
			box.justFlipped = false;

			if (level.isClientSide())
				continue;

			for (Entry<BlockPos, ConnectedPort> portEntry : loopPorts.entrySet()) {
				ConnectedPort port = portEntry.getValue();
				float offBranchAngle = port.chainPosition();

				if (!loopThresholdCrossed(box.chainPosition, prevChainPosition, offBranchAngle))
					continue;
				if (!PackageItem.matchAddress(box.item, port.filter()))
					continue;
				if (!exportToPort(box, portEntry.getKey()))
					continue;

				iterator.remove();
				notifyUpdate();
				continue Looping;
			}

			for (BlockPos connection : connections) {
				float offBranchAngle = connectionStats.get(connection).tangentAngle;

				if (!loopThresholdCrossed(box.chainPosition, prevChainPosition, offBranchAngle))
					continue;
				if (!routingTable.getExitFor(box.item)
					.equals(connection))
					continue;

				box.chainPosition = 0;
				addTravellingPackage(box, connection);
				iterator.remove();
				continue Looping;
			}
		}

		updateBoxWorldPositions();
	}

	public boolean loopThresholdCrossed(float chainPosition, float prevChainPosition, float offBranchAngle) {
		int sign1 = Mth.sign(AngleHelper.getShortestAngleDiff(offBranchAngle, prevChainPosition));
		int sign2 = Mth.sign(AngleHelper.getShortestAngleDiff(offBranchAngle, chainPosition));
		boolean notCrossed = sign1 >= sign2 && !reversed || sign1 <= sign2 && reversed;
		return !notCrossed;
	}

	private boolean exportToPort(ChainLiftPackage box, BlockPos offset) {
		BlockPos globalPos = worldPosition.offset(offset);
		if (!(level.getBlockEntity(globalPos) instanceof PackagePortBlockEntity ppbe))
			return false;

		ppbe.inventory.receiveMode(true);
		ItemStack remainder = ItemHandlerHelper.insertItem(ppbe.inventory, box.item.copy(), false);
		ppbe.inventory.receiveMode(false);

		return remainder.isEmpty();
	}

	public boolean addTravellingPackage(ChainLiftPackage box, BlockPos connection) {
		if (!connections.contains(connection))
			return false;
		travellingPackages.computeIfAbsent(connection, $ -> new ArrayList<>())
			.add(box);
		notifyUpdate();
		return true;
	}

	@Override
	public void notifyUpdate() {
		level.blockEntityChanged(worldPosition);
		sendData();
	}

	public boolean addLoopingPackage(ChainLiftPackage box) {
		loopingPackages.add(box);
		notifyUpdate();
		return true;
	}

	public void prepareStats() {
		float speed = getSpeed();
		if (reversed != speed < 0 && speed != 0) {
			reversed = speed < 0;
			connectionStats = null;
		}
		if (connectionStats == null) {
			connectionStats = new HashMap<>();
			connections.forEach(this::calculateConnectionStats);
		}
	}

	public void updateBoxWorldPositions() {
		prepareStats();

		for (Entry<BlockPos, List<ChainLiftPackage>> entry : travellingPackages.entrySet()) {
			BlockPos target = entry.getKey();
			ConnectionStats stats = connectionStats.get(target);
			for (ChainLiftPackage box : entry.getValue())
				box.worldPosition = stats.start.add((stats.end.subtract(stats.start)).normalize()
					.scale(Math.min(stats.chainLength, box.chainPosition)));
		}

		for (ChainLiftPackage box : loopingPackages)
			box.worldPosition = Vec3.atBottomCenterOf(worldPosition)
				.add(VecHelper.rotate(new Vec3(0, 0.25, 1), box.chainPosition, Axis.Y));
	}

	private void tickBoxVisuals(ChainLiftPackage box) {
		if (box.worldPosition == null)
			return;

		ChainLiftPackagePhysicsData physicsData = box.physicsData(level);
		if (!physicsData.shouldTick())
			return;

		physicsData.prevPos = physicsData.pos;
		if (physicsData.pos.distanceToSqr(box.worldPosition) > 1.5f * 1.5f)
			physicsData.pos = box.worldPosition.add(physicsData.pos.subtract(box.worldPosition)
				.normalize()
				.scale(1.5));
		physicsData.motion = physicsData.motion.add(0, -0.1, 0)
			.scale(0.85)
			.add((box.worldPosition.subtract(physicsData.pos)).scale(0.15));
		physicsData.pos = physicsData.pos.add(physicsData.motion);
	}

	private void calculateConnectionStats(BlockPos connection) {
		boolean reversed = getSpeed() < 0;
		float offBranchDistance = 49f;
		float direction = Mth.RAD_TO_DEG * (float) Mth.atan2(connection.getX(), connection.getZ());
		float angle = wrapAngle(direction - offBranchDistance * (reversed ? -1 : 1));
		float oppositeAngle = wrapAngle(angle + 180 + 2 * 49 * (reversed ? -1 : 1));
		Vec3 start = Vec3.atBottomCenterOf(worldPosition)
			.add(VecHelper.rotate(new Vec3(0, 0, 1), angle, Axis.Y))
			.add(0, 4 / 16f, 0);
		Vec3 end = Vec3.atBottomCenterOf(worldPosition.offset(connection))
			.add(VecHelper.rotate(new Vec3(0, 0, 1), oppositeAngle, Axis.Y))
			.add(0, 4 / 16f, 0);
		float length = (float) start.distanceTo(end);
		connectionStats.put(connection, new ConnectionStats(angle, length, start, end));
	}

	public boolean addConnectionTo(BlockPos target) {
		BlockPos localTarget = target.subtract(worldPosition);
		boolean added = connections.add(localTarget);
		if (added) {
			notifyUpdate();
			calculateConnectionStats(localTarget);
			updateChainShapes();
		}

		detachKinetics();
		updateSpeed = true;

		return added;
	}

	public boolean removeConnectionTo(BlockPos target) {
		BlockPos localTarget = target.subtract(worldPosition);
		boolean removed = connections.remove(localTarget);
		if (removed) {
			connectionStats.remove(localTarget);
			List<ChainLiftPackage> packages = travellingPackages.remove(localTarget);
			if (packages != null)
				for (ChainLiftPackage box : packages)
					drop(box);
			notifyUpdate();
			updateChainShapes();
		}

		detachKinetics();
		updateSpeed = true;

		return removed;
	}

	private void updateChainShapes() {
		List<ChainLiftShape> shapes = new ArrayList<>();
		shapes.add(new ChainLiftBB(Vec3.atBottomCenterOf(BlockPos.ZERO)));
		for (BlockPos target : connections) {
			ConnectionStats stats = connectionStats.get(target);
			if (stats == null)
				continue;
			Vec3 localStart = stats.start.subtract(Vec3.atLowerCornerOf(worldPosition));
			Vec3 localEnd = stats.end.subtract(Vec3.atLowerCornerOf(worldPosition));
			shapes.add(new ChainLiftOBB(target, localStart, localEnd));
		}
		ChainLiftInteractionHandler.loadedChains.get(level)
			.put(worldPosition, shapes);
	}

	@Override
	public void destroy() {
		super.destroy();
		ChainLiftInteractionHandler.loadedChains.get(level)
			.invalidate(worldPosition);

		if (level.isClientSide())
			return;

		for (BlockPos blockPos : connections)
			if (level.getBlockEntity(worldPosition.offset(blockPos)) instanceof ChainLiftBlockEntity clbe)
				clbe.removeConnectionTo(worldPosition);

		for (ChainLiftPackage box : loopingPackages)
			drop(box);
		for (Entry<BlockPos, List<ChainLiftPackage>> entry : travellingPackages.entrySet())
			for (ChainLiftPackage box : entry.getValue())
				drop(box);
	}

	private void drop(ChainLiftPackage box) {
		level.addFreshEntity(PackageEntity.fromItemStack(level, box.worldPosition.subtract(0, 0.5, 0), box.item));
	}

	@Override
	public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
		connections.forEach(p -> neighbours.add(worldPosition.offset(p)));
		return super.addPropagationLocations(block, state, neighbours);
	}

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		if (connections.contains(target.getBlockPos()
			.subtract(worldPosition))) {
			if (!(target instanceof ChainLiftBlockEntity clbe))
				return 0;
			return 1;
		}
		return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("Connections", NBTHelper.writeCompoundList(connections, NbtUtils::writeBlockPos));
		compound.put("TravellingPackages", NBTHelper.writeCompoundList(travellingPackages.entrySet(), entry -> {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("Target", NbtUtils.writeBlockPos(entry.getKey()));
			compoundTag.put("Packages", NBTHelper.writeCompoundList(entry.getValue(),
				clientPacket ? ChainLiftPackage::writeToClient : ChainLiftPackage::write));
			return compoundTag;
		}));
		compound.put("LoopingPackages", NBTHelper.writeCompoundList(loopingPackages,
			clientPacket ? ChainLiftPackage::writeToClient : ChainLiftPackage::write));
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		connections.clear();
		NBTHelper.iterateCompoundList(compound.getList("Connections", Tag.TAG_COMPOUND),
			c -> connections.add(NbtUtils.readBlockPos(c)));
		travellingPackages.clear();
		NBTHelper.iterateCompoundList(compound.getList("TravellingPackages", Tag.TAG_COMPOUND),
			c -> travellingPackages.put(NbtUtils.readBlockPos(c.getCompound("Target")),
				NBTHelper.readCompoundList(c.getList("Packages", Tag.TAG_COMPOUND), ChainLiftPackage::read)));
		loopingPackages =
			NBTHelper.readCompoundList(compound.getList("LoopingPackages", Tag.TAG_COMPOUND), ChainLiftPackage::read);
		connectionStats = null;
		updateBoxWorldPositions();
		updateChainShapes();
	}

	public float wrapAngle(float angle) {
		angle %= 360;
		if (angle < 0)
			angle += 360;
		return angle;
	}

	public List<ChainLiftPackage> getLoopingPackages() {
		return loopingPackages;
	}

	public Map<BlockPos, List<ChainLiftPackage>> getTravellingPackages() {
		return travellingPackages;
	}

}
