package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.AllPackets;

import net.minecraft.core.BlockPos;

public class PackagePortTargetSelectionHandler {

	public static PackagePortTarget activePackageTarget;

	public static void flushSettings(BlockPos pos) {
		if (activePackageTarget == null)
			return;

		activePackageTarget.relativePos = activePackageTarget.relativePos.subtract(pos);
		AllPackets.getChannel()
			.sendToServer(new PackagePortPlacementPacket(activePackageTarget, pos));
		activePackageTarget = null;
		return;
	}

}
