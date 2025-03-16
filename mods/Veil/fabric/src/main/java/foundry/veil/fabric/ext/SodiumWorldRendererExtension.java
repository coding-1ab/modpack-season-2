package foundry.veil.fabric.ext;

import net.caffeinemc.mods.sodium.client.render.chunk.ChunkUpdateType;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;

import java.util.ArrayDeque;
import java.util.Map;

public interface SodiumWorldRendererExtension {

    SortedRenderLists veil$getSortedRenderLists();

    Map<ChunkUpdateType, ArrayDeque<RenderSection>> veil$getTaskLists();

    void veil$setSortedRenderLists(SortedRenderLists sortedRenderLists);

    void veil$setTaskLists(Map<ChunkUpdateType, ArrayDeque<RenderSection>> taskLists);
}
