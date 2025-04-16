package org.antarcticgardens.cna.rendering.fallbackInstance;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.impl.task.ParallelTaskExecutor;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.antarcticgardens.cna.CreateNewAge;

import java.util.HashMap;
import java.util.Map;

public class FallbackInstanceRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final ParallelTaskExecutor taskEngine =
//            IDK if this is a good number of threads
            new ParallelTaskExecutor("Create: New Age Fallback Renderer", 2);
    
    private final Map<T, BlockEntityRenderer<T>> instances = new HashMap<>();
    private final Map<T, SimpleTickableVisual> tickableVisuals = new HashMap<>();
    private final Map<T, DynamicVisual> dynamicVisuals = new HashMap<>();

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
            for (SimpleTickableVisual tickableVisual : tickableVisuals.values())
                tickableVisual.tick();
        }
    }

    private void onRenderLayer(RenderLayerEvent event) {
        if (!VisualizationManager.supportsVisualization((event.getWorld()))) {
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
            for (DynamicVisual dynamicVisual : dynamicVisuals.values())
                dynamicVisual.planFrame();
        }
    }

    private void onReloadRenderers(ReloadLevelRendererEvent event) {
        reset();
    }
    
    private void reset() {
        instances.forEach((t, instance) -> internalRemove(t));
        instances.clear();
        materialManager.delete();
        materialManager = new FallbackMaterialManager();
    }
    
    public void createInstance(T blockEntity) {
        BlockEntityRenderer<T> instance = (BlockEntityRenderer<T>) InstancedRenderRegistry.createInstance(materialManager, blockEntity);
        
        if (instance == null) 
            throw new RuntimeException("Cannot create instance for " + blockEntity.getType());

        ((BlockEntityExtension) blockEntity).create_new_age$setFallbackInstance(this, instance);
        
        instance.init();
        instance.updateLight();
        LightUpdater.get(blockEntity.getLevel()).addListener(instance);

        instances.put(blockEntity, instance);
        
        if (instance instanceof SimpleTickableVisual tickable)
            tickableVisuals.put(blockEntity, tickable);

        if (instance instanceof DynamicVisual dynamic)
            dynamicVisuals.put(blockEntity, dynamic);
    }
    
    public void removeInstance(BlockEntity blockEntity) {
        internalRemove(blockEntity);
        instances.remove(blockEntity);
    }
    
    private void internalRemove(BlockEntity blockEntity) {
        BlockEntityVisual<?> instance = ((BlockEntityExtension) blockEntity).create_new_age$getFallbackInstance();
        ((BlockEntityExtension) blockEntity).create_new_age$setFallbackInstance(null, null);
        instance.removeAndMark();
        tickableVisuals.remove(blockEntity);
        dynamicVisuals.remove(blockEntity);
        LightUpdater.get(blockEntity.getLevel()).removeListener(instance);
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!VisualizationManager.supportsVisualization(blockEntity.getLevel())) {
            if (!instances.containsKey(blockEntity) && !blockEntity.isRemoved()) 
                createInstance(blockEntity);
        } 
    } 
    
    public static TaskExecutor getTaskEngine() {
        return taskEngine;
    }
}
