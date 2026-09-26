package org.antarcticgardens.cna.content.electricity.network;

import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkTicker {
    private static final Map<ServerLevel, List<ElectricalNetwork>> networks = new HashMap<>();

    public static void addNetwork(ServerLevel level, ElectricalNetwork network) {
        List<ElectricalNetwork> networkList = networks.computeIfAbsent(level, (ignored) -> new ArrayList<>());

        if (!networkList.contains(network))
            networkList.add(network);

        networks.put(level, networkList);
    }

    public static void removeNetwork(ElectricalNetwork network) {
        networks.values().forEach(list -> list.remove(network));
    }

    public static void onLevelUnload(ServerLevel unloaded) {
        networks.remove(unloaded);
    }

    public static void tickPre(ServerLevel world) {
        List<ElectricalNetwork> networkList = networks.get(world);
        if (networkList == null) {
            return;
        }
        networkList.removeIf(ElectricalNetwork::isEmpty);

        for (ElectricalNetwork network : networkList) {
            network.tickPre();
        }
    }

    public static void tickPost(ServerLevel level) {
        List<ElectricalNetwork> networkList = networks.get(level);
        if (networkList == null) {
            return;
        }

        for (ElectricalNetwork network : networkList)
            network.tickPost(level);
    }
}
