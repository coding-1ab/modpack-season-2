package org.antarcticgardens.newage.content.electricity.network;

import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.newage.content.electricity.connector.ElectricalConnectorBlockEntity;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ElectricalNetwork {
    private final List<ElectricalConnectorBlockEntity> nodes = new ArrayList<>();
    private final Map<ElectricalConnectorBlockEntity, BotariumEnergyBlock<?>> consumers = new HashMap<>();

    private final ElectricalNetworkPathManager pathManager = new ElectricalNetworkPathManager();

    public ElectricalNetwork(ElectricalConnectorBlockEntity base) {
        nodes.add(base);
    }

    public void addNode(ElectricalConnectorBlockEntity node) {
        addNode(node, new ArrayList<>());
        updateConsumers();
    }

    private void addNode(ElectricalConnectorBlockEntity node, List<ElectricalConnectorBlockEntity> processedNodes) {
        if (!nodes.contains(node))
            nodes.add(node);

        processedNodes.add(node);

        if (node.getNetwork() != this) {
            if (node.getNetwork() != null)
                node.getNetwork().destroy();

            node.setNetwork(this);

            for (ElectricalConnectorBlockEntity connector : node.getConnectors().keySet()) {
                if (!processedNodes.contains(connector))
                    addNode(connector, processedNodes);
            }
        }

        for (ElectricalConnectorBlockEntity connectedNode : node.getConnectors().keySet())
            pathManager.addConnection(node, connectedNode);
    }

    public void updateConsumers() {
        consumers.clear();

        for (ElectricalConnectorBlockEntity node : nodes) {
            if (node.getLevel() != null) {
                BlockEntity entity = node.getLevel().getBlockEntity(node.getSupportingBlockPos());

                if (entity instanceof BotariumEnergyBlock<?> energyBlock && !(entity instanceof ElectricalConnectorBlockEntity)) {
                    if (energyBlock.getEnergyStorage().allowsInsertion())
                        consumers.put(node, energyBlock);
                }
            }
        }

        if (!getWorld().isClientSide())
            ElectricalNetworkTicker.addNetwork(this);
    }

    public long insert(ElectricalConnectorBlockEntity from, long amount, boolean simulate) {
        if (consumers.isEmpty())
            return 0;

        AtomicLong inserted = new AtomicLong();
        long perConsumer = (long) Math.ceil(amount / (double) consumers.size());

        NetworkPathConductivityContext context = simulate ?
                new NetworkPathConductivityContext(pathManager.getConductivityContext()) :
                pathManager.getConductivityContext();

        consumers.entrySet().stream()
                .sorted(Comparator.comparingDouble(e ->
                        e.getValue().getEnergyStorage().getStoredEnergy() / (double) e.getValue().getEnergyStorage().getMaxCapacity()))
                .takeWhile(e -> inserted.get() < amount)
                .forEach(e -> inserted.addAndGet(insertInto(from, e, context, Math.min(perConsumer, amount - inserted.get()), simulate)));

        return inserted.get();
    }

    private long insertInto(ElectricalConnectorBlockEntity from,
                            Map.Entry<ElectricalConnectorBlockEntity, BotariumEnergyBlock<?>> to,
                            NetworkPathConductivityContext context,
                            long amount,
                            boolean simulate) {
        NetworkPath path;
        long inserted = 0;

        while ((path = pathManager.findConductiblePath(from, to.getKey())) != null && inserted < amount) {
            long pathConductivity = context.calculatePathConductivity(path);
            long insertedThroughThisPath = to.getValue().getEnergyStorage().insertEnergy(Math.min(pathConductivity, amount - inserted), simulate);

            if (insertedThroughThisPath == 0)
                break;

            if (!simulate && insertedThroughThisPath > 0 && to.getValue() instanceof BlockEntity be) {
                be.setChanged();
                if (be.getLevel() instanceof ServerLevel serverLevel)
                    serverLevel.getChunkSource().blockChanged(be.getBlockPos());
            }

            inserted += insertedThroughThisPath;

            if (pathConductivity != 0)
                context.decreasePathConductivity(path, insertedThroughThisPath);
        }

        return inserted;
    }

    public void destroy() {
        ElectricalNetworkTicker.removeNetwork(this);
        for (ElectricalConnectorBlockEntity node : nodes)
            node.setNetwork(new ElectricalNetwork(node));
    }

    protected void tick() {
        pathManager.tick();
    }

    public Level getWorld() {
        return nodes.get(0).getLevel();
    }

    public List<ElectricalConnectorBlockEntity> getNodes() {
        return Collections.unmodifiableList(nodes);
    }
}
