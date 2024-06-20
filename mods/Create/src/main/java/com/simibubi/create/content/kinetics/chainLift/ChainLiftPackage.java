package com.simibubi.create.content.kinetics.chainLift;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.WorldAttached;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class ChainLiftPackage {

	// Server creates unique ids for chain boxes
	public static final AtomicInteger netIdGenerator = new AtomicInteger();

	// Client tracks physics data by id so it can travel between BEs
	private static final int ticksUntilExpired = 30;
	public static final WorldAttached<Cache<Integer, ChainLiftPackagePhysicsData>> physicsDataCache =
		new WorldAttached<>($ -> CacheBuilder.newBuilder()
			.expireAfterAccess(ticksUntilExpired * 50, TimeUnit.MILLISECONDS)
			.build());

	public class ChainLiftPackagePhysicsData {
		public Vec3 targetPos;
		public Vec3 prevTargetPos;
		public Vec3 prevPos;
		public Vec3 pos;
		
		public Vec3 motion;
		public int lastTick;
		public float yaw;
		public float prevYaw;
		public boolean flipped;
		public ResourceLocation modelKey;

		public ChainLiftPackagePhysicsData(Vec3 serverPosition) {
			this.targetPos = null;
			this.prevTargetPos = null;
			this.pos = null;
			this.prevPos = null;
			
			this.motion = Vec3.ZERO;
			this.lastTick = AnimationTickHolder.getTicks();
		}

		public boolean shouldTick() {
			if (lastTick == AnimationTickHolder.getTicks())
				return false;
			lastTick = AnimationTickHolder.getTicks();
			return true;
		}
	}

	public float chainPosition;
	public ItemStack item;
	public int netId;
	public boolean justFlipped;

	public Vec3 worldPosition;
	public float yaw;

	private ChainLiftPackagePhysicsData physicsData;

	public ChainLiftPackage(float chainPosition, ItemStack item) {
		this(chainPosition, item, netIdGenerator.incrementAndGet());
	}

	public ChainLiftPackage(float chainPosition, ItemStack item, int netId) {
		this.chainPosition = chainPosition;
		this.item = item;
		this.netId = netId;
		this.physicsData = null;
	}

	public CompoundTag writeToClient() {
		CompoundTag tag = write();
		tag.putInt("NetID", netId);
		return tag;
	}

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putFloat("Position", chainPosition);
		compoundTag.put("Item", item.serializeNBT());
		return compoundTag;
	}

	public static ChainLiftPackage read(CompoundTag compoundTag) {
		float pos = compoundTag.getFloat("Position");
		ItemStack item = ItemStack.of(compoundTag.getCompound("Item"));
		if (compoundTag.contains("NetID"))
			return new ChainLiftPackage(pos, item, compoundTag.getInt("NetID"));
		return new ChainLiftPackage(pos, item);
	}

	public ChainLiftPackagePhysicsData physicsData(LevelAccessor level) {
		if (physicsData == null) {
			try {
				return physicsData = physicsDataCache.get(level)
					.get(netId, () -> new ChainLiftPackagePhysicsData(worldPosition));
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		physicsDataCache.get(level)
			.getIfPresent(netId);
		return physicsData;
	}

}
