package org.antarcticgardens.cna.content.electricity.network;

import net.minecraft.util.Tuple;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorBlockEntity;
import org.antarcticgardens.cna.util.HashSortedPair;

import java.util.HashMap;
import java.util.Map;

public class NetworkPathConductivityContext {
    private final Map<HashSortedPair<ElectricalConnectorBlockEntity>, Tuple<Long, Long>> connections;

    public NetworkPathConductivityContext() {
        connections = new HashMap<>();
    }

    public NetworkPathConductivityContext(NetworkPathConductivityContext context) {
        connections = new HashMap<>();

        for (Map.Entry<HashSortedPair<ElectricalConnectorBlockEntity>, Tuple<Long, Long>> e : context.connections.entrySet())
            connections.put(e.getKey(), new Tuple<>(e.getValue().getA(), e.getValue().getB()));
    }

    public void addConnection(ElectricalConnectorBlockEntity node, ElectricalConnectorBlockEntity node1) {
        if (!node.equals(node1) && !connections.containsKey(new HashSortedPair<>(node, node1)))
            connections.put(new HashSortedPair<>(node, node1), new Tuple<>(node.getConnectedConnectors().get(node1).getConductivity(), 0L));
    }

    protected long calculatePathConductivity(NetworkPath path) {
        ElectricalConnectorBlockEntity prevNode = null;
        long conductivity = Long.MAX_VALUE;

        for (ElectricalConnectorBlockEntity node : path.getNodes()) {
            if (prevNode == null) {
                prevNode = node;
                continue;
            }

            HashSortedPair<ElectricalConnectorBlockEntity> key = new HashSortedPair<>(prevNode, node);

            if (!connections.containsKey(key))
                return 0;

            long connectionConductivity = connections.get(key).getB();
            conductivity = Math.min(connectionConductivity, conductivity);
            prevNode = node;
        }

        return conductivity;
    }

    protected void decreasePathConductivity(NetworkPath path, long amount) {
        ElectricalConnectorBlockEntity prevNode = null;

        for (ElectricalConnectorBlockEntity node : path.getNodes()) {
            if (prevNode == null) {
                prevNode = node;
                continue;
            }

            HashSortedPair<ElectricalConnectorBlockEntity> key = new HashSortedPair<>(prevNode, node);
            long connectionConductivity = connections.get(key).getB();
            connections.get(key).setB(connectionConductivity - amount);
            prevNode = node;
        }
    }

    protected long getConnectionConductivity(HashSortedPair<ElectricalConnectorBlockEntity> key) {
        return connections.get(key).getB();
    }

    protected void updateConductivity() {
        for (Map.Entry<HashSortedPair<ElectricalConnectorBlockEntity>, Tuple<Long, Long>> e : connections.entrySet())
            connections.get(e.getKey()).setB(e.getValue().getA());
    }
}
