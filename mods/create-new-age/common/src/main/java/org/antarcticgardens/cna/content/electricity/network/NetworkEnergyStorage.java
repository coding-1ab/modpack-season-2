package org.antarcticgardens.cna.content.electricity.network;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.cna.content.electricity.connector.AbstractElectricalConnector;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.transaction.SnapshotParticipant;
import org.antarcticgardens.esl.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class NetworkEnergyStorage extends SnapshotParticipant<Object> implements EnergyStorage {
    private @NonNull ElectricalNetwork network;

    public NetworkEnergyStorage(@NonNull ElectricalNetwork network) {
        this.network = network;
    }
    
    public @NonNull ElectricalNetwork getNetwork() {
        return network;
    }
    
    public void setNetwork(@NonNull ElectricalNetwork network) {
        this.network = network;
    }

    @Override
    public long insert(long maxAmount, TransactionContext txn) {
        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext txn) {
        return 0;
    }

    @Override
    public long getStoredEnergy() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public Object createSnapshot() {
        return network.createSnapshot();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void restoreSnapshot(Object object) {
        if (object instanceof NetworkSnapshot snapshot) {
            for (Map.Entry<AbstractElectricalConnector, Object> e : snapshot.getSnapshots().entrySet()) {
                EnergyStorage storage = EnergyStorage.findForBlock(e.getKey().getLevel(), e.getKey().getSupportingBlockPos(),
                        e.getKey().getBlockState().getValue(BlockStateProperties.FACING));
                
                if (storage instanceof SnapshotParticipant sp) {
                    sp.restoreSnapshot(e.getValue());
                }
            }
        }
    }
}
