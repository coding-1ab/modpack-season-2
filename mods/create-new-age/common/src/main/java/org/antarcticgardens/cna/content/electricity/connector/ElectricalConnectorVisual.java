package org.antarcticgardens.cna.content.electricity.connector;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.antarcticgardens.cna.config.CNAConfig;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

public class ElectricalConnectorVisual extends AbstractBlockEntityVisual<ElectricalConnectorBlockEntity> implements SimpleDynamicVisual {
    private static final Map<ElectricalConnectorBlockEntity, ElectricalConnectorVisual> instances = new HashMap<>();

    private final Map<BlockPos, Wire> wires = new HashMap<>();
    private final Map<BlockPos, List<Pair<Instance, Vec3>>> wireInstances = new HashMap<>();

    public ElectricalConnectorVisual(VisualizationContext ctx, ElectricalConnectorBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
        instances.put(blockEntity, this);
        updateConnections();
    }

    private void updateConnections() {
        wires.entrySet().removeIf(e -> {
            boolean remove = !blockEntity.isConnected(e.getKey());
            if (remove) 
                removeWireInstances(e.getKey());
            return remove;
        });

        blockEntity.getConnectorPositions().entrySet().stream().filter(e -> !wires.containsKey(e.getKey()))
                .forEach(e -> createConnection(e.getKey(), e.getValue()));
    }

    private void createConnection(BlockPos target, WireType wireType) {
        if (getVisualPosition().hashCode() > target.hashCode()) {
            Vector3f direction = blockPosToVector3f(target.subtract(getVisualPosition()));
            float distance = direction.length();
            int sectionsAmount = (int) Math.ceil(CNAConfig.getClient().wireSectionsPerMeter.get() * distance);
            direction.normalize();
            Wire wire = new Wire(direction, distance, sectionsAmount);
            wireInstances.put(target, createWireInstances(wire, getVisualPosition().getCenter(), wireType.getTextureLocation()));
            wires.put(target, wire);
        }
    }
    
    private List<Pair<Instance, Vec3>> createWireInstances(Wire wire, Vec3 position, ResourceLocation texture) {
        List<Pair<Instance, Vec3>> instances = new ArrayList<>();
        
        for (int i = 0; i < wire.getSections().size(); i++) {
            Pair<WireSection, Float> pair = wire.getSections().get(i);

            TransformedInstance data = instancerProvider().solid(CNARenderTypes.wire(texture))
                    .material(Materials.TRANSFORMED)
                    .model(pair.first().name(), () -> pair.first())
                    .createInstance();

            PoseStack ps = new PoseStack();
            TransformStack ts = TransformStack.cast(ps);
            ts.translate(position);
            ps.mulPoseMatrix(new Matrix4f().rotateTowards(wire.getDirection(), wire.getUp()));
            ts.translate(0.0f, pair.getSecond(), wire.getSectionLength() * i);
            data.setTransform(ps);

            instances.add(Pair.of(data, new Vec3(wire.getDirection()).scale(wire.getSectionLength() * i)
                    .add(new Vec3(wire.getUp()).scale(pair.getSecond()))));
        }
        
        return instances;
    }
    
    private void removeWireInstances(BlockPos target) {
        wireInstances.getOrDefault(target, new ArrayList<>()).forEach(p -> p.getFirst().delete());
    }

    private Vector3f blockPosToVector3f(BlockPos pos) {
        return new Vector3f(pos.getX(), pos.getY(), pos.getZ());
    }

    public static ElectricalConnectorVisual get(ElectricalConnectorBlockEntity connector) {
        return instances.get(connector);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {

    }

    @Override
    public void beginFrame(Context context) {
        if (blockEntity.needsInstanceUpdate) {
            updateConnections();
            blockEntity.needsInstanceUpdate = false;
        }
    }

    @Override
    public Plan<Context> planFrame() {
        return null;
    }

    @Override
    public void updateLight(float partialTick) {
        wires.forEach((k, w) ->
                wireInstances.forEach((blockPos, pairs) ->
                        pairs.forEach(p ->
                                relight(BlockPos.containing(this.getVisualPosition().getCenter().add(p.getSecond())), p.getFirst()))));
    }

    @Override
    protected void _delete() {

    }
}
