package org.antarcticgardens.cna.rendering.fallbackInstance;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.instancing.BatchDrawingTracker;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class FallbackMaterialManager implements MaterialManager {
    private final Map<RenderLayer, Map<RenderType, FallbackMaterialGroup>> layers = new EnumMap<>(RenderLayer.class);
    
    private final BatchDrawingTracker tracker = new BatchDrawingTracker();
    private final TaskEngine taskEngine = FallbackInstanceRenderer.getTaskEngine();
    
    public FallbackMaterialManager() {
        for (RenderLayer value : RenderLayer.values()) 
            layers.put(value, new HashMap<>());
    }
    
    @Override
    public FallbackMaterialGroup state(RenderLayer layer, RenderType state) {
        return layers.get(layer).computeIfAbsent(state, FallbackMaterialGroup::new);
    }

    @Override
    public Vec3i getOriginCoordinate() {
        return Vec3i.ZERO;
    }
    
    public void delete() {
        for (Map<RenderType, FallbackMaterialGroup> groups : layers.values()) {
            groups.forEach((renderType, group) -> group.delete());
        }
    }
    
    public void render(RenderLayer layer, PoseStack poseStack) {
        Map<RenderType, FallbackMaterialGroup> groups = layers.get(layer);
        for (FallbackMaterialGroup group : groups.values()) 
            group.render(poseStack, taskEngine, tracker);
        
        taskEngine.syncPoint();
        tracker.endBatch();
    }
}
