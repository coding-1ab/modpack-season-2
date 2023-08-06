package org.antarcticgardens.newage.content.electricity.network;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import org.antarcticgardens.newage.content.electricity.connector.ElectricalConnectorBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElectricalNetworkPathManager {
    public static final int MAX_PATHFINDING_DEPTH = 128;

    private final Map<ElectricalConnectorBlockEntity, BotariumEnergyBlock<?>> consumers;

    private final Map<NetworkPathKey<ElectricalConnectorBlockEntity>, List<NetworkPath>> paths = new HashMap<>();
    private final NetworkPathConductivityContext context = new NetworkPathConductivityContext();

    protected ElectricalNetworkPathManager(Map<ElectricalConnectorBlockEntity, BotariumEnergyBlock<?>> consumers) {
        this.consumers = consumers;
    }

    protected void addConnection(ElectricalConnectorBlockEntity node, ElectricalConnectorBlockEntity node1) {
        context.addConnection(node, node1);
    }

    protected List<NetworkPath> findPaths(ElectricalConnectorBlockEntity a, ElectricalConnectorBlockEntity b) {
        NetworkPath path = new NetworkPath(a);
        return findPaths(path, b, 0);
    }

    private List<NetworkPath> findPaths(NetworkPath path, ElectricalConnectorBlockEntity target, int depth) {
        if (paths.containsKey(new NetworkPathKey<>(path.getLastNode(), target)))
            return paths.get(new NetworkPathKey<>(path.getLastNode(), target));

        List<NetworkPath> foundPaths = new ArrayList<>();

        for (ElectricalConnectorBlockEntity connector : path.getLastNode().getConnectors().keySet()) {
            if (path.getNodes().contains(connector))
                continue;

            NetworkPath connectorPath = new NetworkPath(path);
            connectorPath.addNode(connector);

            if (connector == target) {
                foundPaths.add(new NetworkPath(connectorPath));
                continue;
            }

            if (consumers.containsKey(connector)) {
                List<NetworkPath> pathList = paths.getOrDefault(new NetworkPathKey<>(path.getFirstNode(), path.getLastNode()), new ArrayList<>());

                if (!pathList.contains(connectorPath)) {
                    pathList.add(new NetworkPath(connectorPath));
                    paths.put(new NetworkPathKey<>(path.getFirstNode(), path.getLastNode()), pathList);
                }
            }

            if (depth++ <= MAX_PATHFINDING_DEPTH)
                foundPaths.addAll(findPaths(connectorPath, target, depth));
        }

        return foundPaths;
    }

    protected NetworkPathConductivityContext getConductivityContext() {
        return context;
    }

    protected void tick() {
        context.updateConductivity();
    }
}
