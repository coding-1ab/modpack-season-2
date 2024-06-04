package com.simibubi.create.content.kinetics.chainLift;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.createmod.catnip.utility.WorldAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class ChainLiftInteractionHandler {

	public static WorldAttached<Cache<BlockPos, List<ChainLiftShape>>> loadedChains =
		new WorldAttached<>($ -> CacheBuilder.newBuilder()
			.expireAfterAccess(3, TimeUnit.SECONDS)
			.build());

	public static BlockPos selectedLift;
	public static float selectedChainPosition;
	public static BlockPos selectedConnection;

	public static void clientTick() {
		if (!isActive()) {
			selectedLift = null;
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		double range = mc.player.getAttribute(ForgeMod.BLOCK_REACH.get())
			.getValue() + 1;
		Vec3 from = RaycastHelper.getTraceOrigin(mc.player);
		Vec3 to = RaycastHelper.getTraceTarget(mc.player, range, from);
		HitResult hitResult = mc.hitResult;

		double bestDiff = Float.MAX_VALUE;
		if (hitResult != null)
			bestDiff = hitResult.getLocation()
				.distanceToSqr(from);

		BlockPos bestLift = null;
		ChainLiftShape bestShape = null;
		selectedConnection = null;

		for (Entry<BlockPos, List<ChainLiftShape>> entry : loadedChains.get(Minecraft.getInstance().level)
			.asMap()
			.entrySet()) {
			BlockPos liftPos = entry.getKey();
			for (ChainLiftShape chainLiftShape : entry.getValue()) {
				Vec3 liftVec = Vec3.atLowerCornerOf(liftPos);
				Vec3 intersect = chainLiftShape.intersect(from.subtract(liftVec), to.subtract(liftVec));
				if (intersect == null)
					continue;

				double distanceToSqr = intersect.add(liftVec)
					.distanceToSqr(from);
				if (distanceToSqr > bestDiff)
					continue;
				bestDiff = distanceToSqr;
				bestLift = liftPos;
				bestShape = chainLiftShape;
				selectedChainPosition = chainLiftShape.getChainPosition(intersect);
				if (chainLiftShape instanceof ChainLiftShape.ChainLiftOBB obb)
					selectedConnection = obb.connection;
			}
		}

		selectedLift = bestLift;
		if (bestLift == null)
			return;

		bestShape.drawOutline(bestLift);
		bestShape.drawPoint(bestLift, selectedChainPosition);
	}

	private static boolean isActive() {
		Minecraft mc = Minecraft.getInstance();
		ItemStack mainHandItem = mc.player.getMainHandItem();
		return AllItemTags.WRENCH.matches(mainHandItem) || AllBlocks.PACKAGE_PORT.isIn(mainHandItem);
	}

	public static boolean onUse() {
		if (selectedLift == null)
			return false;

		Minecraft mc = Minecraft.getInstance();
		ItemStack mainHandItem = mc.player.getMainHandItem();

		if (AllItemTags.WRENCH.matches(mainHandItem)) {
			if (!mc.player.isCrouching()) {
				ChainLiftRidingHandler.embark(selectedLift, selectedChainPosition, selectedConnection);
				return true;
			}
			// dismantle or start riding
			return true;
		}

		if (AllBlocks.PACKAGE_PORT.isIn(mainHandItem)) {
			PackagePortTargetSelectionHandler.activePackageTarget =
				new PackagePortTarget.ChainLiftPortTarget(selectedLift, selectedChainPosition, selectedConnection);
			return true;
		}

		return false;
	}

}
