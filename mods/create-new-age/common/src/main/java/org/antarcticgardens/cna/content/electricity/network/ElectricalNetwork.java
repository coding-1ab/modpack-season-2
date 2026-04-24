package org.antarcticgardens.cna.content.electricity.network;

import net.createmod.catnip.data.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.cna.content.electricity.connector.AbstractElectricalConnector;
import org.antarcticgardens.cna.content.electricity.connector.IElectricityNode;
import org.antarcticgardens.cna.content.electricity.wire.Wire;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.antarcticgardens.esl.energy.EnergyStorage;
import org.antarcticgardens.esl.transaction.Transaction;
import org.antarcticgardens.esl.transaction.TransactionStack;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.flow.DinicMFImpl;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElectricalNetwork {
    private static final Logger LOGGER = LogManager.getLogger();
    private final SimpleWeightedGraph<IElectricityNode, Wire> graph = new SimpleWeightedGraph<>(Wire.class);
    private final Map<AbstractElectricalConnector, EnergyBlockEntity> consumers = new HashMap<>();
    private final Map<AbstractElectricalConnector, EnergyBlockEntity> pulledSources = new HashMap<>();
    private final Map<AbstractElectricalConnector, TransactionRequest> consumeRequests = new HashMap<>();
    private final Map<AbstractElectricalConnector, TransactionRequest> pullRequests = new HashMap<>();

    public Stream<Pair<IElectricityNode, Wire>> getNeighbours(IElectricityNode base) {
        if (!graph.containsVertex(base)) {
            return Stream.empty();
        }

        return graph.edgesOf(base).stream().map(edge -> {
            var src = graph.getEdgeSource(edge);
            var dst = graph.getEdgeTarget(edge);
            var other = src == base ? dst : src;

            return Pair.of(other, edge);
        });
    }

    public void mergeOther(ElectricalNetwork other) {
        if (graph == other.graph) {
            return;
        }
        Graphs.addGraph(graph, other.graph);

        other.graph.vertexSet().forEach(node -> node.setNetwork(this));
        NetworkTicker.removeNetwork(other);
    }

    public void addNode(IElectricityNode node) {
        graph.addVertex(node);
    }

    public void removeNode(AbstractElectricalConnector node) {
        if (!(node.getLevel() instanceof ServerLevel level)) {
            return;
        }

        var neighbours = getNeighbours(node).toList();
        if (graph.removeVertex(node)) {
            separateNetworks(level);
            neighbours.forEach(neighbour -> {
                if (!(neighbour.getFirst() instanceof AbstractElectricalConnector connector)) {
                    return;
                }

                connector.setChanged();
                connector.sendToClient();
            });
        }
    }

    public void addConnection(IElectricityNode from, IElectricityNode to, WireType wireType) {
        Wire wire = Wire.create(wireType, from.getBlockPos(), to.getBlockPos());
        boolean added = this.graph.addEdge(from, to, wire);
        this.graph.setEdgeWeight(wire, wire.type().getConductivity());

        if (added) {
            from.onNearConnectionChanged();
            to.onNearConnectionChanged();

            ElectricalNetwork other = from.getNetwork();
            if (other == this) {
                other = to.getNetwork();
            }

            mergeOther(other);

            if (from instanceof AbstractElectricalConnector blockEntity) {
                blockEntity.setChanged();
                blockEntity.sendToClient();
            }
            if (to instanceof AbstractElectricalConnector blockEntity) {
                blockEntity.setChanged();
                blockEntity.sendToClient();
            }
        }
    }

    public void disconnect(AbstractElectricalConnector from, AbstractElectricalConnector to) {
        if (!(from.getLevel() instanceof ServerLevel level)) {
            return;
        }
        graph.removeEdge(from, to);
        separateNetworks(level);
        if (from instanceof AbstractElectricalConnector blockEntity) {
            blockEntity.setChanged();
            blockEntity.sendToClient();
        }
        if (to instanceof AbstractElectricalConnector blockEntity) {
            blockEntity.setChanged();
            blockEntity.sendToClient();
        }
    }

    public Wire getEdge(IElectricityNode from, IElectricityNode to) {
        return graph.getEdge(from, to);
    }

    private void separateNetworks(ServerLevel level) {
        ConnectivityInspector<IElectricityNode, Wire> inspector = new ConnectivityInspector<>(graph);
        List<Set<IElectricityNode>> components = inspector.connectedSets();

        if (components.size() == 1) {
            return;
        }

        for (Set<IElectricityNode> component : components) {
            ElectricalNetwork newNetwork = new ElectricalNetwork();
            component.forEach(newNetwork.graph::addVertex);
            graph.edgeSet().forEach(wire -> {
                var source = graph.getEdgeSource(wire);
                var target = graph.getEdgeTarget(wire);

                // Only need to check one of these 2 to see if edge should be copied or not
                if (newNetwork.graph.containsVertex(source)) {
                    newNetwork.graph.addEdge(source, target, wire);
                    newNetwork.graph.setEdgeWeight(wire, wire.type().getConductivity());
                }
            });

            newNetwork.graph.vertexSet().forEach(node -> node.setNetwork(newNetwork));
            NetworkTicker.addNetwork(level, newNetwork);
        }
        NetworkTicker.removeNetwork(this);
    }

    public void notifySource(AbstractElectricalConnector source, EnergyBlockEntity target) {
        pulledSources.put(source, target);
    }

    public void notifySink(AbstractElectricalConnector sink, EnergyBlockEntity target) {
        consumers.put(sink, target);
    }

    private void transferPower() {
        var triples = List.of(
                Triple.of(consumers, consumeRequests, false),
                Triple.of(pulledSources, pullRequests, true)
        );

        for (var triple : triples) {
            Map<AbstractElectricalConnector, EnergyBlockEntity> requester = triple.getLeft();
            Map<AbstractElectricalConnector, TransactionRequest> recorder = triple.getMiddle();
            boolean isInput = triple.getRight();

            for (Map.Entry<AbstractElectricalConnector, EnergyBlockEntity> e : requester.entrySet()) {
                try (Transaction txn = TransactionStack.get().openOuter()) {
                    AbstractElectricalConnector connector = e.getKey();
                    EnergyBlockEntity blockEntity = e.getValue();
                    EnergyStorage storage = blockEntity.storage();

                    long amount;
                    if (isInput) {
                        amount = storage.extract(Long.MAX_VALUE, txn);
                    } else {
                        amount = storage.insert(Long.MAX_VALUE, txn);
                    }
                    if (amount == 0) {
                        continue;
                    }

                    TransactionRequest request = new TransactionRequest(amount, connector, blockEntity.storage());
                    recorder.put(e.getKey(), request);
                }
            }
        }

        consumers.clear();
        pulledSources.clear();

        if (consumeRequests.isEmpty() || pullRequests.isEmpty()) {
            return;
        }

        // This fake graph is to turn multi-source multi-sink problem into single-source single-sink problem
        var fakeGraph = new SimpleWeightedGraph<IElectricityNode, Wire>(Wire.class);
        var superSource = new IElectricityNode.FakeNode(true);
        var superSink = new IElectricityNode.FakeNode(false);
        fakeGraph.addVertex(superSource);
        fakeGraph.addVertex(superSink);

        consumeRequests.forEach((input, request) -> {
            fakeGraph.addVertex(input);
            // BlockPosition isn't relevant here, but we provide it anyway since it provides good enough identity for Wire
            Wire edge = Wire.create(WireType.OVERCHARGED_DIAMOND, superSource.getBlockPos(), input.getBlockPos());
            fakeGraph.addEdge(superSource, input, edge);
            fakeGraph.setEdgeWeight(edge, request.amount);
        });

        pullRequests.forEach((output, request) -> {
            fakeGraph.addVertex(output);

            Wire edge = Wire.create(WireType.OVERCHARGED_DIAMOND, superSink.getBlockPos(), output.getBlockPos());
            fakeGraph.addEdge(superSink, output, edge);
            fakeGraph.setEdgeWeight(edge, request.amount);
        });
        var union = new AsGraphUnion<>(fakeGraph, graph);

        // Dinic's algorithm has O(V * E^2) time complexity
        // Edmond's algorithm has O(V^2 * E) time complexity
        // Since we have more edges than vertices we use Dinic's algorithm
        MaximumFlowAlgorithm<IElectricityNode, Wire> algorithm = new DinicMFImpl<>(union);

        var maxFlow = algorithm.getMaximumFlow(superSource, superSink);
        var flowMap = maxFlow.getFlowMap();

        consumeRequests.forEach((consume, request) -> {
            Wire edge = union.getEdge(superSource, consume);
            Double flow = flowMap.get(edge);
            long toInsert = 0;
            if (flow != null) {
                toInsert = flow.longValue();
            }
            if (toInsert == 0) {
                return;
            }

            try (Transaction txn = TransactionStack.get().openOuter()) {
                long inserted = request.target.insert(toInsert, txn);
                if (inserted < toInsert) {
                    LOGGER.warn("Energy storage lied and did not take all energy from network. Lost {} FE", toInsert - inserted);
                }
                txn.commit();
            }
        });

        pullRequests.forEach((pull, request) -> {
            Wire edge = union.getEdge(superSink, pull);
            Double flow = flowMap.get(edge);
            long toExtract = 0;
            if (flow != null) {
                toExtract = flow.longValue();
            }
            if (toExtract == 0) {
                return;
            }

            try (Transaction txn = TransactionStack.get().openOuter()) {
                long extracted = request.target.extract(toExtract, txn);
                if (extracted < toExtract) {
                    LOGGER.warn("Energy storage lied and did not provide enought energy into network. Created {} FE", toExtract - extracted);
                }
                txn.commit();
            }
        });
    }

    protected boolean isEmpty() {
        return graph.vertexSet().isEmpty();
    }

    protected void tickPre() {

    }

    protected void tickPost(ServerLevel level) {
        level.getProfiler().push("camera");
        transferPower();
        level.getProfiler().pop();
        consumeRequests.clear();
        pullRequests.clear();
    }

    public NetworkSnapshot createSnapshot() {
        return new NetworkSnapshot(graph);
    }

    private record TransactionRequest(
            long amount,
            AbstractElectricalConnector connector,
            EnergyStorage target
    ) {
    }
}
