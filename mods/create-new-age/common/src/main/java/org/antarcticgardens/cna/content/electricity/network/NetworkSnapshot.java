package org.antarcticgardens.cna.content.electricity.network;

import org.antarcticgardens.cna.content.electricity.connector.AbstractElectricalConnector;
import org.antarcticgardens.cna.content.electricity.connector.IElectricityNode;
import org.antarcticgardens.cna.content.electricity.wire.Wire;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.transaction.SnapshotParticipant;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

public class NetworkSnapshot {
    private final Map<AbstractElectricalConnector, Object> snapshots = new HashMap<>();

    public NetworkSnapshot(Graph<IElectricityNode, Wire> network) {
        for (IElectricityNode node : network.vertexSet()) {
            if (!(node instanceof AbstractElectricalConnector connector)) {
                return;
            }

            EnergyStorage storage = EnergyStorage.findForBlock(connector.getLevel(), connector.getSupportingBlockPos(), 
                    connector.getFacing());
            
            if (storage instanceof SnapshotParticipant<?> snapshotParticipant && !(storage instanceof NetworkEnergyStorage)) {
                snapshots.put(connector, snapshotParticipant.createSnapshot());
            }
        }
    }
    
    public Map<AbstractElectricalConnector, Object> getSnapshots() {
        return snapshots;
    }
}
