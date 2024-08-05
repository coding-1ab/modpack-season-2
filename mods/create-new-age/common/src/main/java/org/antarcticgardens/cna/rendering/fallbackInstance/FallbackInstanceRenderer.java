package org.antarcticgardens.cna.rendering.fallbackInstance;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.ParallelTaskEngine;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.light.LightUpdater;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.cna.CreateNewAge;

import java.util.HashMap;
import java.util.Map;

public class FallbackInstanceRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final ParallelTaskEngine taskEngine = new ParallelTaskEngine("Create: New Age Fallback Renderer");
    
    private final Map<T, BlockEntityInstance<T>> instances = new HashMap<>();
    private final Map<T, TickableInstance> tickableInstances = new HashMap<>();
    private final Map<T, DynamicInstance> dynamicInstances = new HashMap<>();

    private FallbackMaterialManager materialManager = new FallbackMaterialManager();
    private boolean enabled = false;
    
    public FallbackInstanceRenderer(BlockEntityRendererProvider.Context context) {
        CreateNewAge.getInstance().getPlatform().subscribeClientTickEnd(this::onClientTickEnd);
        CreateNewAge.getInstance().getPlatform().subscribeRenderLayerEvent(this::onRenderLayer);
        CreateNewAge.getInstance().getPlatform().subscribeBeginFrameEvent(this::onBeginFrame);
        CreateNewAge.getInstance().getPlatform().subscribeReloadRenderersEvent(this::onReloadRenderers);
    }

    private void onClientTickEnd() {
        if (Backend.isGameActive()) {
            for (TickableInstance tickableInstance : tickableInstances.values())
                tickableInstance.tick();
        }
    }

    private void onRenderLayer(RenderLayerEvent event) {
        if (!Backend.canUseInstancing(event.getWorld())) {
            if (!enabled) {
                taskEngine.startWorkers();
                enabled = true;
            }
            
            if (event.layer != null) {
                event.stack.pushPose();
                event.stack.translate(-event.camX, -event.camY, -event.camZ);
                materialManager.render(event.layer, event.stack);
                event.stack.popPose();
            }
        } else {
            if (enabled) {
                taskEngine.stopWorkers();
                reset();
                enabled = false;
            }
        }
    }
    
    private void onBeginFrame(BeginFrameEvent event) {
        if (Backend.isGameActive()) {
            for (DynamicInstance dynamicInstance : dynamicInstances.values())
                dynamicInstance.beginFrame();
        }
    }

    private void onReloadRenderers(ReloadRenderersEvent event) {
        reset();
    }
    
    private void reset() {
        instances.forEach((t, instance) -> internalRemove(t));
        instances.clear();
        materialManager.delete();
        materialManager = new FallbackMaterialManager();
    }
    
    public void createInstance(T blockEntity) {
        BlockEntityInstance<T> instance = (BlockEntityInstance<T>) InstancedRenderRegistry.createInstance(materialManager, blockEntity);
        
        if (instance == null) 
            throw new RuntimeException("Cannot create instance for " + blockEntity.getType());

        ((BlockEntityExtension) blockEntity).create_new_age$setFallbackInstance(this, instance);
        
        instance.init();
        instance.updateLight();
        LightUpdater.get(blockEntity.getLevel()).addListener(instance);

        instances.put(blockEntity, instance);
        
        if (instance instanceof TickableInstance tickable) 
            tickableInstances.put(blockEntity, tickable);

        if (instance instanceof DynamicInstance dynamic)
            dynamicInstances.put(blockEntity, dynamic);
    }
    
    public void removeInstance(BlockEntity blockEntity) {
        internalRemove(blockEntity);
        instances.remove(blockEntity);
    }
    
    private void internalRemove(BlockEntity blockEntity) {
        BlockEntityInstance<?> instance = ((BlockEntityExtension) blockEntity).create_new_age$getFallbackInstance();
        ((BlockEntityExtension) blockEntity).create_new_age$setFallbackInstance(null, null);
        instance.removeAndMark();
        tickableInstances.remove(blockEntity);
        dynamicInstances.remove(blockEntity);
        LightUpdater.get(blockEntity.getLevel()).removeListener(instance);
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!Backend.canUseInstancing(blockEntity.getLevel())) {
            if (!instances.containsKey(blockEntity) && !blockEntity.isRemoved()) 
                createInstance(blockEntity);
        } 
    } 
    
    public static TaskEngine getTaskEngine() {
        return taskEngine;
    }
}
