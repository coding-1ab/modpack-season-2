package net.caffeinemc.mods.sodium.client.render.chunk.lists;

import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.fabric.ext.RenderRegionExtension;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.core.SectionPos;

public class PerspectiveChunkCollector implements OcclusionCuller.Visitor {

    private final ObjectArrayList<ChunkRenderList> sortedRenderLists;
    private static int[] sortItems = new int[256];

    public PerspectiveChunkCollector() {
        this.sortedRenderLists = new ObjectArrayList<>();
    }

    @Override
    public void visit(RenderSection section) {
        if (section.getFlags() != 0) {
            RenderRegion region = section.getRegion();
            RenderRegionExtension ext = (RenderRegionExtension) region;
            ChunkRenderList renderList = ext.veil$getPerspectiveRenderList();
            if (renderList.getLastVisibleFrame() != VeilLevelPerspectiveRenderer.getID()) {
                renderList.reset(VeilLevelPerspectiveRenderer.getID());
                this.sortedRenderLists.add(renderList);
            }

            renderList.add(section);
        }
    }

    public SortedRenderLists createRenderLists(Viewport viewport) {
        SectionPos sectionPos = viewport.getChunkCoord();
        int cameraX = sectionPos.getX() >> RenderRegion.REGION_WIDTH_SH;
        int cameraY = sectionPos.getY() >> RenderRegion.REGION_HEIGHT_SH;
        int cameraZ = sectionPos.getZ() >> RenderRegion.REGION_LENGTH_SH;
        int size = this.sortedRenderLists.size();
        if (sortItems.length < size) {
            sortItems = new int[size];
        }

        for (int i = 0; i < size; i++) {
            RenderRegion region = this.sortedRenderLists.get(i).getRegion();
            int x = Math.abs(region.getX() - cameraX);
            int y = Math.abs(region.getY() - cameraY);
            int z = Math.abs(region.getZ() - cameraZ);
            sortItems[i] = x + y + z << 16 | i;
        }

        IntArrays.unstableSort(sortItems, 0, size);
        ObjectArrayList<ChunkRenderList> sorted = new ObjectArrayList<>(size);

        for (int i = 0; i < size; i++) {
            int key = sortItems[i];
            ChunkRenderList renderList = this.sortedRenderLists.get(key & 65535);
            sorted.add(renderList);
        }

        for (ChunkRenderList list : sorted) {
            list.sortSections(sectionPos, sortItems);
        }

        return new SortedRenderLists(sorted);
    }
}
