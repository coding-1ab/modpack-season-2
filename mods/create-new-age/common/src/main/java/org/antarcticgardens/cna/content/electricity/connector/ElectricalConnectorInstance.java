package org.antarcticgardens.cna.content.electricity.connector;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.antarcticgardens.cna.CNARenderTypes;
import org.antarcticgardens.cna.config.CNAConfig;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.antarcticgardens.cna.util.HashSortedPair;
import org.joml.Vector3f;

import java.util.*;

public class ElectricalConnectorInstance extends BlockEntityInstance<ElectricalConnectorBlockEntity> implements DynamicInstance {
    private static final Map<ElectricalConnectorBlockEntity, ElectricalConnectorInstance> instances = new HashMap<>();

    private final Map<BlockPos, ModelData> wires = new HashMap<>();

    public ElectricalConnectorInstance(MaterialManager materialManager, ElectricalConnectorBlockEntity connector) {
        super(materialManager, connector);
        instances.put(connector, this);
        updateConnections();
    }

    public void updateConnections() {
        wires.entrySet().removeIf(e -> {
            boolean remove = !blockEntity.isConnected(e.getKey());

            if (remove) {
                e.getValue().delete();
            }

            return remove;
        });

        blockEntity.getConnectorPositions().entrySet().stream().filter(e -> !wires.containsKey(e.getKey()))
                .forEach(e -> createConnection(e.getKey(), e.getValue()));

        updateLight();
    }

    private void createConnection(BlockPos target, WireType wireType) {
        if (getWorldPosition().hashCode() > target.hashCode()) {
            Vector3f direction = blockPosToVector3f(target.subtract(getWorldPosition()));
            float distance = direction.length();
            direction.normalize();

            int sections = (int) Math.ceil(CNAConfig.getClient().wireSectionsPerMeter.get() * distance);
            float sectionLength = distance / sections;

            ModelData data = materialManager.solid(CNARenderTypes.wire(wireType.getTextureLocation()))
                    .material(Materials.TRANSFORMED)
                    .model(new HashSortedPair<>(getWorldPosition(), target), () ->
                            new WireModel(direction, sections, sectionLength, CNAConfig.getClient().wireThickness.get().floatValue()))
                    .createInstance();

            PoseStack ps = new PoseStack();
            TransformStack ts = TransformStack.cast(ps);
            ts.translate(getInstancePosition().getCenter());
            data.setTransform(ps);

            wires.put(target, data);
        }
    }

    @Override
    protected void remove() {
        instances.remove(blockEntity);
        wires.forEach((k, v) -> v.delete());
        wires.clear();
    }

    @Override
    public void beginFrame() {
        if (blockEntity.needsInstanceUpdate) {
            updateConnections();
            blockEntity.needsInstanceUpdate = false;
        }
    }

    @Override
    public void updateLight() {
        wires.forEach((k, v) -> relight(getWorldPosition(), v));
    }

    private Vector3f blockPosToVector3f(BlockPos pos) {
        return new Vector3f(pos.getX(), pos.getY(), pos.getZ());
    }

    public static ElectricalConnectorInstance get(ElectricalConnectorBlockEntity connector) {
        return instances.get(connector);
    }
}
