package org.antarcticgardens.cna.content.electricity.network;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.cna.content.electricity.connector.AbstractElectricalConnector;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.transaction.SnapshotParticipant;
import org.antarcticgardens.esl.transaction.TransactionContext;

import java.util.Map;

public class SimpleNetworkEnergyStorage extends SnapshotParticipant<Object> implements EnergyStorage {
    private long capacity;
    private long stored = 0;
    private boolean supportsInsertion = true;
    private boolean supportsExtraction = true;
    private long maxExtract = Long.MAX_VALUE;
    private long maxInsert = Long.MAX_VALUE;

    private Runnable finalCommitCallback = () -> {};

    private final AbstractElectricalConnector connector;
    private ElectricalNetwork network;

    public SimpleNetworkEnergyStorage(AbstractElectricalConnector connector, ElectricalNetwork network, long capacity) {
        this.connector = connector;
        this.network = network;

        if (capacity < 0) {
            throw new IllegalArgumentException("SimpleEnergyStorage capacity can't be negative");
        }

        this.capacity = capacity;
    }

    public ElectricalNetwork getNetwork() {
        return network;
    }

    public void setNetwork(ElectricalNetwork network) {
        this.network = network;
    }

    public SimpleNetworkEnergyStorage onFinalCommit(Runnable callback) {
        finalCommitCallback = callback;
        return this;
    }

    public SimpleNetworkEnergyStorage setCapacity(long capacity) {
        this.capacity = capacity;
        stored = Math.min(stored, capacity);

        return this;
    }

    public SimpleNetworkEnergyStorage setSupportsInsertion(boolean supportsInsertion) {
        this.supportsInsertion = supportsInsertion;
        return this;
    }

    public SimpleNetworkEnergyStorage setSupportsExtraction(boolean supportsExtraction) {
        this.supportsExtraction = supportsExtraction;
        return this;
    }

    public SimpleNetworkEnergyStorage setMaxExtract(long maxExtract) {
        this.maxExtract = maxExtract;
        return this;
    }

    public SimpleNetworkEnergyStorage setMaxInsert(long maxInsert) {
        this.maxInsert = maxInsert;
        return this;
    }

    public SimpleNetworkEnergyStorage setStoredEnergy(long amount) {
        stored = Math.max(0, Math.min(amount, capacity));
        return this;
    }

    @Override
    protected void onFinalCommit() {
        finalCommitCallback.run();
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getStoredEnergy() {
        return stored;
    }

    @Override
    public Object createSnapshot() {
        if (network == null)
            return null;

        return new NetworkSnapshot(network);
    }

    @Override
    public long insert(long amount, TransactionContext context) {
        if (!supportsInsertion) {
            return 0;
        }

        updateSnapshots(context);

        long inserted = Math.min(Math.min(amount, maxInsert), capacity - stored);
        stored += inserted;

        return inserted;
    }

    @Override
    public long extract(long amount, TransactionContext context) {
        if (!supportsExtraction) {
            return 0;
        }

        updateSnapshots(context);

        long extracted = Math.min(Math.min(amount, maxExtract), stored);
        stored -= extracted;

        return extracted;
    }

    public long internalInsert(long amount, boolean simulated) {
        long inserted = Math.min(amount, capacity - stored);

        if (simulated) {
            return inserted;
        }

        stored += inserted;
        return inserted;
    }

    public long internalExtract(long amount, boolean simulated) {
        long extracted = Math.min(amount, stored);

        if (simulated) {
            return extracted;
        }

        stored -= extracted;
        return extracted;
    }

    @Override
    public boolean supportsExtraction() {
        return supportsExtraction;
    }

    @Override
    public boolean supportsInsertion() {
        return supportsInsertion;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void restoreSnapshot(Object object) {
        if (object instanceof NetworkSnapshot snapshot) {
            getNetwork().getPathManager().setConductivityContext(new NetworkPathConductivityContext(snapshot.getContext()));

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
