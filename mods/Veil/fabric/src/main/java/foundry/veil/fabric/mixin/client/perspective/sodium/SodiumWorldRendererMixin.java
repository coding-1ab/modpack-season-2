package foundry.veil.fabric.mixin.client.perspective.sodium;

import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.fabric.ext.SodiumWorldRendererExtension;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkUpdateType;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayDeque;
import java.util.Map;

@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin implements SodiumWorldRendererExtension {

    @Shadow(remap = false)
    private RenderSectionManager renderSectionManager;

    @ModifyVariable(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;updateCameraState(Lorg/joml/Vector3dc;Lnet/minecraft/client/Camera;)V"), ordinal = 2, remap = false)
    public boolean setCameraLocationChanged(boolean value) {
        return !VeilLevelPerspectiveRenderer.isRenderingPerspective() && value;
    }

    @Override
    public SortedRenderLists veil$getSortedRenderLists() {
        return this.renderSectionManager.getRenderLists();
    }

    @Override
    public Map<ChunkUpdateType, ArrayDeque<RenderSection>> veil$getTaskLists() {
        return ((RenderSectionManagerAccessor) this.renderSectionManager).getTaskLists();
    }

    @Override
    public void veil$setSortedRenderLists(SortedRenderLists sortedRenderLists) {
        ((RenderSectionManagerAccessor) this.renderSectionManager).setRenderLists(sortedRenderLists);
    }

    @Override
    public void veil$setTaskLists(Map<ChunkUpdateType, ArrayDeque<RenderSection>> taskLists) {
        ((RenderSectionManagerAccessor) this.renderSectionManager).setTaskLists(taskLists);
    }
}
