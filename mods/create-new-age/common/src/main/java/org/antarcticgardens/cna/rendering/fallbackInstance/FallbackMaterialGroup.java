package org.antarcticgardens.cna.rendering.fallbackInstance;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.BatchDrawingTracker;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.Map;

public class FallbackMaterialGroup implements MaterialGroup {
    private final RenderType state;
    
    private final Map<Batched<? extends InstanceData>, FallbackMaterial<?>> materials = new HashMap<>();
    
    private int instanceCount = 0;
    private int vertexCount = 0;

    public FallbackMaterialGroup(RenderType state) {
        this.state = state;
    }

    @Override
    public <D extends InstanceData> FallbackMaterial<D> material(StructType<D> type) {
        if (type instanceof Batched<D> batched) {
            return (FallbackMaterial<D>) materials.computeIfAbsent(batched, FallbackMaterial::new);
        } else {
            throw new ClassCastException("Cannot use type '" + type + "' with fallback instance renderer.");
        }
    }
    
    public void delete() {
        materials.values().forEach(FallbackMaterial::delete);
    }
    
    public void render(PoseStack poseStack, TaskEngine taskEngine, BatchDrawingTracker source) {
        instanceCount = 0;
        vertexCount = 0;

        for (FallbackMaterial<?> material : materials.values()) {
            for (FallbackInstancer<?> instancer : material.getModels().values()) {
                instancer.setup();
                instanceCount += instancer.getInstanceCount();
                vertexCount += instancer.getVertexCount();
            }
        }
        
        DirectVertexConsumer consumer = source.getDirectConsumer(state, vertexCount);
        consumer.memSetZero();
        
        for (FallbackMaterial<?> material : materials.values()) {
            for (FallbackInstancer<?> instancer : material.getModels().values()) 
                instancer.submitTask(poseStack, taskEngine, consumer);
        }
    }
}
