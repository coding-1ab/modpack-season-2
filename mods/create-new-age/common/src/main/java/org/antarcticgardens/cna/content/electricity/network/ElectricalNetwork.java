package org.antarcticgardens.cna.content.electricity.network;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorBlock;
import org.antarcticgardens.cna.content.electricity.connector.ElectricalConnectorBlockEntity;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.transaction.Transaction;
import org.antarcticgardens.esl.transaction.TransactionContext;
import org.antarcticgardens.esl.transaction.TransactionStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ElectricalNetwork {
    private final List<ElectricalConnectorBlockEntity> nodes = new ArrayList<>();
    private final Map<ElectricalConnectorBlockEntity, EnergyBlockEntity> consumers = new HashMap<>();
    private final Map<ElectricalConnectorBlockEntity, EnergyBlockEntity> pulledSources = new HashMap<>();

    private final NetworkPathManager pathManager = new NetworkPathManager();

    public ElectricalNetwork(ElectricalConnectorBlockEntity base) {
        nodes.add(base);
    }

    public void addNode(ElectricalConnectorBlockEntity node) {
        addNode(node, new ArrayList<>());
        updateConsumersAndSources();
    }

    private void addNode(ElectricalConnectorBlockEntity node, List<ElectricalConnectorBlockEntity> processedNodes) {
        if (!nodes.contains(node))
            nodes.add(node);

        processedNodes.add(node);

        if (node.getNetwork() != this) {
            if (node.getNetwork() != null)
                node.getNetwork().destroy();

            node.setNetwork(this);

            for (ElectricalConnectorBlockEntity connector : node.getConnectedConnectors().keySet()) {
                if (!processedNodes.contains(connector))
                    addNode(connector, processedNodes);
            }
        }

        for (ElectricalConnectorBlockEntity connectedNode : node.getConnectedConnectors().keySet())
            pathManager.addConnection(node, connectedNode);
    }

    public void updateConsumersAndSources() {
        consumers.clear();
        pulledSources.clear();

        for (ElectricalConnectorBlockEntity node : nodes) {
            if (node.getLevel() != null) {
                Direction dir = node.getBlockState().getValue(BlockStateProperties.FACING);
                BlockEntity entity = node.getLevel().getBlockEntity(node.getSupportingBlockPos());
                
                if (entity != null && !(entity instanceof ElectricalConnectorBlockEntity)) {
                    EnergyStorage storage = EnergyStorage.findForBlock(node.getLevel(), node.getSupportingBlockPos(), dir);
                    
                    if (storage != null) {
                        if (storage.supportsInsertion()) {
                            consumers.put(node, new EnergyBlockEntity(entity, storage));
                        }
                        
                        if (storage.supportsExtraction() && node.getBlockState().getValue(ElectricalConnectorBlock.MODE).pull) {
                            pulledSources.put(node, new EnergyBlockEntity(entity, storage));
                        }
                    }
                }
            }
        }

        if (!getWorld().isClientSide())
            NetworkTicker.addNetwork(this);
    }

    public long insert(ElectricalConnectorBlockEntity from, long amount, TransactionContext txn) {
        if (consumers.isEmpty())
            return 0;
        
        AtomicLong inserted = new AtomicLong();
        long perConsumer = (long) Math.ceil(amount / (double) (consumers.size()));
        
        // TODO: Bring sorting back?
        
        boolean insertedAny = true;
        
        while (inserted.get() < amount && insertedAny) {
            insertedAny = false;
            
            for (Map.Entry<ElectricalConnectorBlockEntity, EnergyBlockEntity> e : consumers.entrySet()) {
                if (e.getKey().equals(from)) {
                    continue;
                }
                
                long ins = insertInto(from, e, Math.min(perConsumer, amount - inserted.get()), txn);
                
                if (ins > 0) {
                    insertedAny = true;
                    inserted.addAndGet(ins);
                }
            }
        }
        
        return inserted.get();
    }

    private long insertInto(ElectricalConnectorBlockEntity from,
                            Map.Entry<ElectricalConnectorBlockEntity, EnergyBlockEntity> to,
                            long amount, TransactionContext txn) {
        NetworkPath path;
        long inserted = 0;

        while ((path = pathManager.findConductiblePath(from, to.getKey())) != null && inserted < amount) {
            long pathConductivity = pathManager.getConductivityContext().calculatePathConductivity(path);
            long insertedThroughThisPath = to.getValue().storage().insert(Math.min(pathConductivity, amount - inserted), txn);

            if (insertedThroughThisPath == 0)
                break;

            if (insertedThroughThisPath > 0) {
                BlockEntity be = to.getValue().entity();

                be.setChanged();
                if (be.getLevel() instanceof ServerLevel serverLevel)
                    serverLevel.getChunkSource().blockChanged(be.getBlockPos());
            } // TODO: Move to final commit?

            inserted += insertedThroughThisPath;

            if (pathConductivity != 0)
                pathManager.getConductivityContext().decreasePathConductivity(path, insertedThroughThisPath);
        }

        return inserted;
    }

    public void destroy() {
        NetworkTicker.removeNetwork(this);
        for (ElectricalConnectorBlockEntity node : nodes)
            node.setNetwork(new ElectricalNetwork(node));
    }

    protected void tick() {
        for (Map.Entry<ElectricalConnectorBlockEntity, EnergyBlockEntity> e : pulledSources.entrySet()) {
            try (Transaction txn = TransactionStack.get().openOuter(); Transaction test = txn.openNested()) {
                long maxExtracted = e.getValue().storage().extract(Long.MAX_VALUE, test);
                test.abort();
                long inserted = insert(e.getKey(), maxExtracted, txn);
                e.getValue().storage().extract(inserted, txn);
                txn.commit();
            }
        }
        
        pathManager.tick();
    }

    public Level getWorld() {
        return nodes.get(0).getLevel();
    }

    public List<ElectricalConnectorBlockEntity> getNodes() {
        return Collections.unmodifiableList(nodes);
    }
    
    public NetworkPathManager getPathManager() {
        return pathManager;
    }
}
