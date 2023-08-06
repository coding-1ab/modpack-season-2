package org.antarcticgardens.newage.content.electricity.network;

import org.antarcticgardens.newage.content.electricity.connector.ElectricalConnectorBlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkPath {
    private final List<ElectricalConnectorBlockEntity> nodes = new ArrayList<>();

    public NetworkPath(ElectricalConnectorBlockEntity baseNode) {
        addNode(baseNode);
    }

    public NetworkPath(NetworkPath base) {
        for (ElectricalConnectorBlockEntity node : base.getNodes())
            addNode(node);
    }

    public void addNode(ElectricalConnectorBlockEntity node) {
        if (!nodes.contains(node))
            nodes.add(node);
    }

    public ElectricalConnectorBlockEntity getFirstNode() {
        return nodes.get(0);
    }

    public ElectricalConnectorBlockEntity getLastNode() {
        return nodes.get(nodes.size() - 1);
    }

    public List<ElectricalConnectorBlockEntity> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public int hashCode() {
        return nodes.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NetworkPath path)
            return nodes.equals(path.nodes);

        return false;
    }
}
