package com.hlysine.create_connected.content.dashboard;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.hlysine.create_connected.content.dashboard.DashboardBlockEntity.CYCLE_INTERVAL;
import static com.hlysine.create_connected.content.dashboard.DashboardBlockEntity.LAZY_TICK_RATE;

public class DashboardClientLogic {
    private static boolean displayStatus(DashboardBlockEntity dashboard) {
        BlockPos seatPos = dashboard.getSeatPos();
        if (seatPos == null)
            return false;

        Player player = Minecraft.getInstance().player;
        if (player == null)
            return false;
        if (!player.isPassenger())
            return false;

        Vec3 center = Vec3.atCenterOf(seatPos);
        if (player.distanceToSqr(center) > 1.2)
            return false;
        List<Component> list = dashboard.getAllDisplays(seatPos);
        if (list == null || list.isEmpty()) return false;

        Component status = list.get((dashboard.cycleTimer / CYCLE_INTERVAL) % list.size());
        player.displayClientMessage(status, true);
        dashboard.cycleTimer += LAZY_TICK_RATE;
        return true;
    }

    static void tryDisplay(DashboardBlockEntity dashboard) {
        boolean success = displayStatus(dashboard);
        if (!success && dashboard.wasDisplaying) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                if (!dashboard.getBlockState().getValue(DashboardBlock.OPEN))
                    DashboardBlockEntity.displayOpenStatus(player, false); // avoid flickering on wrench by displaying the open status instead of empty
                else
                    player.displayClientMessage(Component.empty(), true);
            }
        }
        dashboard.wasDisplaying = success;
    }
}
