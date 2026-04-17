package org.antarcticgardens.cna.content.electricity.network;

import org.antarcticgardens.cna.config.CNAConfig;
import org.antarcticgardens.cna.content.electricity.connector.AbstractElectricalConnector;
import org.antarcticgardens.cna.util.HashSortedPair;

import java.util.*;

public class NetworkPathManager {
    private NetworkPathConductivityContext context = new NetworkPathConductivityContext();

    protected void addConnection(AbstractElectricalConnector node, AbstractElectricalConnector node1) {
        context.addConnection(node, node1);
    }

    protected NetworkPath findConductiblePath(AbstractElectricalConnector a, AbstractElectricalConnector b) {
        List<AbstractElectricalConnector> visited = new ArrayList<>();
        Queue<QueueElement> queue = new LinkedList<>();
        queue.add(new QueueElement(a, null, 0));
        visited.add(a);

        while (!queue.isEmpty()) {
            var element = queue.poll();

            if (element.connector.equals(b)) {
                NetworkPath path = unwrapConductiblePath(element);
                if (path != null && context.calculatePathConductivity(path) > 0)
                    return path;
            }

            for (AbstractElectricalConnector connector : element.connector.getConnectedConnectors().keySet()) {
                if (!visited.contains(connector) && element.depth < CNAConfig.getServer().maxPathfindingDepth.get()) {
                    visited.add(connector);
                    queue.add(new QueueElement(connector, element, element.depth + 1));
                }
            }
        }

        return null;
    }

    private NetworkPath unwrapConductiblePath(QueueElement element) {
        NetworkPath path = new NetworkPath();

        while (element != null) {
            if (path.getLength() != 0 && context.getConnectionConductivity(new HashSortedPair<>(element.connector, path.getFirstNode())) <= 0)
                return null;

            path.addNodeToBeginning(element.connector);
            element = element.parent;
        }

        if (path.getLength() < 2)
            return null;

        return path;
    }

    protected NetworkPathConductivityContext getConductivityContext() {
        return context;
    }
    
    protected void setConductivityContext(NetworkPathConductivityContext context) {
        this.context = context;
    }

    protected void tick() {
        context.updateConductivity();
    }

    private record QueueElement(AbstractElectricalConnector connector, QueueElement parent, int depth) { }
}
