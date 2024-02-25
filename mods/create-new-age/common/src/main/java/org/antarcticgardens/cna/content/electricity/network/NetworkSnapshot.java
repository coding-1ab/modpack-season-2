package org.antarcticgardens.cna.content.electricity.network;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorBlockEntity;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.transaction.SnapshotParticipant;

import java.util.HashMap;
import java.util.Map;

public class NetworkSnapshot {
    private final Map<ElectricalConnectorBlockEntity, Object> snapshots = new HashMap<>();
    private final NetworkPathConductivityContext context;
    
    public NetworkSnapshot(ElectricalNetwork network) {
        context = new NetworkPathConductivityContext(network.getPathManager().getConductivityContext());
        
        for (ElectricalConnectorBlockEntity connector : network.getNodes()) {
            EnergyStorage storage = EnergyStorage.findForBlock(connector.getLevel(), connector.getSupportingBlockPos(), 
                    connector.getBlockState().getValue(BlockStateProperties.FACING));
            
            if (storage instanceof SnapshotParticipant<?> snapshotParticipant) {
                snapshots.put(connector, snapshotParticipant.createSnapshot());
            }
        }
    }
    
    public Map<ElectricalConnectorBlockEntity, Object> getSnapshots() {
        return snapshots;
    }
    
    public NetworkPathConductivityContext getContext() {
        return context;
    }
}
